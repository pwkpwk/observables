package com.ambientbytes.observables;

import java.util.Collections;
import java.util.Comparator;

/**
 * Implementation of IReadOnlyObservableList that orders items of another observable list
 * according to an ordering object.
 * @author Pavel Karpenko
 *
 * @param <T> type of the list item.
 */
final class OrderingReadOnlyObservableList<T>
				extends LinkedReadOnlyObservableList<T>
				implements IItemsOrderContainer<T> {

	private final ArrayListEx<ItemContainer> data;
	private IItemsOrder<T> order;
	
	private final class ItemContainer implements IObjectMutationObserver {
		private final T item;
		private IMutableObject mutable;
		
		public ItemContainer(T item) {
			this.item = item;
			if (item instanceof IMutableObject) {
				this.mutable = (IMutableObject) item;
				this.mutable.addObserver(this);
			} else {
				this.mutable = null;
			}
		}
		
		public T item() {
			return item;
		}
		
		public void unadvise() {
			if (mutable != null) {
				mutable.removeObserver(this);
				mutable = null;
			}
		}

		@Override
		public void mutated() {
			onItemMutated(item);
		}
	}

	/**
	 * Construct a new OrderingReadOnlyObservableList object, copy items from the source list,
	 * and subscribe for updates of the source.
	 * @param source source list.
	 * @param order rule object for ordering item in the ordering list.
	 */
	public OrderingReadOnlyObservableList(
			IReadOnlyObservableList<T> source,
			IItemsOrder<T> order,
			IReadWriteMonitor monitor) {
		super(source, monitor);
		this.data = new ArrayListEx<>(source.getSize());
		this.order = order;

		IResource res = monitor.acquireRead();
		
		try {
			final int size = source.getSize();
			
			for (int i = 0; i < size; ++i) {
				this.data.add(new ItemContainer(source.getAt(i)));
			}
			Collections.sort(this.data, makeComparator(order));
		} finally {
			res.release();
		}
	}

	@Override
	public T getAt(int index) {
		return data.get(index).item();
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public IItemsOrder<T> getOrder() {
		return order;
	}

	@Override
	public void setOrder(IItemsOrder<T> order) {
		if (this.order != order) {
			notifyResetting();
			this.order = order;
			Collections.sort(this.data, makeComparator(order));
			notifyReset();
		}
	}

	@Override
	protected void onAdded(IReadOnlyObservableList<T> source, int startIndex, int count) {
		for (int i = 0; i < count; ++i) {
			// TODO: optimize reporting - bundle items up in ranges
			insertAndNotify(source.getAt(startIndex + i));
		}
	}

	@Override
	protected void onChanging(IReadOnlyObservableList<T> source, int startIndex, int count) {
		onRemoving(source, startIndex, count);
	}

	@Override
	protected void onChanged(IReadOnlyObservableList<T> source, int startIndex, int count) {
		onAdded(source, startIndex, count);
	}
	
	@Override
	protected void onRemoving(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		for (int i = startIndex; i < startIndex + count; ++i) {
			final T item = source.getAt(i);
			final int index = indexOfItem(item);
			
			if (index >= 0) {
				ItemContainer container = data.get(index);
				// Unadvise the container before notifying subscribers about the change
				// so if the subscribers will mutate the item in the observer callbacks,
				// the correct item will be removed from data.
				container.unadvise();
				notifyRemoving(index, 1);
				data.remove(index);
				notifyRemoved(index, 1);
			}
		}
	}

	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, int count) {
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		// Do nothing. Moving items in the source collection does not affect their order in the ordered one.
	}
	
	@Override
	protected void onResetting(IReadOnlyObservableList<T> source) {
		notifyResetting();
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source) {
		for (ItemContainer c : data) {
			c.unadvise();
		}
		data.clear();
		
		for (int i = 0; i < source.getSize(); ++i) {
			data.add(new ItemContainer(source.getAt(i)));
		}
		Collections.sort(data, makeComparator(order));
		notifyReset();
	}
	
	@Override
	protected void onUnlinked() {
		for (ItemContainer c : data) {
			c.unadvise();
		}
	}
	
	private void onItemMutated(T item) {
		//
		// Item mutations must be processed under a write lock because they
		// may change the collection that is updated by event handlers that are supposed
		// to be synchronized by the same lock (all collections in the pipeline are supposed
		// to share a single lock).
		//
		IResource lock = monitor().acquireWrite();
		
		try {
			final int oldIndex = indexOfMutatedItem(item);
			//
			// Exclude the mutated item and binary search the new position for the item.
			// If the position has changed, move the item.
			//
			IRandomAccess<ItemContainer> pokedAccess = new PokedListRandomAccess<>(data, oldIndex);
			final int newIndex = indexOfFirstGreaterOrEqualItem(pokedAccess, item);
			
			if (oldIndex != newIndex) {
				data.move(oldIndex, newIndex, 1);
				notifyMoved(oldIndex, newIndex, 1);
			}
		} finally {
			lock.release();
		}
	}
	
	private Comparator<ItemContainer> makeComparator(final IItemsOrder<T> order) {
		return new Comparator<ItemContainer>() {
			@Override
			public int compare(ItemContainer c1, ItemContainer c2) {
				int result = 0;
				
				if (order.isLess(c1.item(), c2.item())) {
					result = -1;
				} else if (order.isLess(c2.item(), c1.item())) {
					result = 1;
				}
				
				return result;
			}
		};
	}
	
	private int indexOfItem(T item) {
		IRandomAccess<ItemContainer> access = new ListRandomAccess<>(data);
		int index = indexOfFirstGreaterOrEqualItem(access, item);
		
		while (index < data.size() && !order.isLess(data.get(index).item(), item)) {
			if (data.get(index).item() == item) {
				break;
			} else {
				++index;
			}
		}
		
		if (index >= data.size()) {
			index = -1;
		}
		
		return index;
	}
	
	private int indexOfMutatedItem(T item) {
		//
		// Must do a linear scan of the data list because we may be looking for a mutated item
		// that went out of order.
		//
		int index = -1;
		
		for (int i = 0; index < 0 && i < data.size(); ++i) {
			if (data.get(i).item() == item) {
				index = i;
			} else {
				++i;
			}
		}
		
		return index;
	}
	
	private int indexOfFirstGreaterOrEqualItem(IRandomAccess<ItemContainer> dataAccess, T item) {
		//
		// Return index of the first item that is greater or equal than the specified item
		// according to the set order.
		// A new item may be inserted at the returned index.
		//
		int left = -1;
		int right = dataAccess.size();
		
		while (left + 1 != right) {
			int middle = left + (right - left) / 2;
			
			if (order.isLess(dataAccess.get(middle).item(), item)) {
				left = middle;
			} else {
				right = middle;
			}
		}
		
		return right;
	}
	
	private void insertAndNotify(T item) {
		IRandomAccess<ItemContainer> access = new ListRandomAccess<>(data);
		int insertionIndex = indexOfFirstGreaterOrEqualItem(access, item);
		data.add(insertionIndex, new ItemContainer(item));
		notifyAdded(insertionIndex, 1);
	}

}
