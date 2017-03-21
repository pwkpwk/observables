package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class OrderingReadOnlyObservableList<T>
				extends LinkedReadOnlyObservableList<T>
				implements IItemsOrderContainer<T> {

	private final List<ItemContainer> data;
	private IItemsOrder<T> order;
	
	private final class ItemContainer implements IObjectMutationObserver {
		private final T item;
		private final IMutableObject mutable;
		
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
			if (mutable != null) {}
			mutable.removeObserver(this);
		}

		@Override
		public void mutated() {
			onItemMutated(item);
		}
	}

	public OrderingReadOnlyObservableList(
			IReadOnlyObservableList<T> source,
			IItemsOrder<T> order) {
		super(source);
		this.data = new ArrayList<>(source.getSize());
		this.order = order;
		
		final int size = source.getSize();
		
		for (int i = 0; i < size; ++i) {
			this.data.add(new ItemContainer(source.getAt(i)));
		}
		Collections.sort(this.data, makeComparator(order));
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
			final int size = data.size();
			
			Collection<T> oldItems = new ArrayList<>(size);
			for (int i = 0; i < size; ++i) {
				oldItems.add(data.get(i).item());
			}
			this.order = order;
			Collections.sort(this.data, makeComparator(order));
			notifyReset(oldItems);
		}
	}

	@Override
	protected void onAdded(IReadOnlyObservableList<T> source, int startIndex, int count) {
		for (int i = 0; i < count; ++i) {
			// TODO: find insertion index and add a new ItemContainer that wraps the new item.
		}
	}

	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, Collection<T> items) {
		for (T item : items) {
			int index = indexOfItem(data, item);
			
			if (index >= 0) {
				List<T> removed = new ArrayList<>(1);
				ItemContainer container = data.get(index);
				data.remove(index);
				container.unadvise();
				removed.add(item);
				notifyRemoved(index, removed);
			}
		}
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		// Do nothing. Moving items in the source collection does not affect their order in the ordered one.
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source, Collection<T> items) {
		Collection<T> oldItems = new ArrayList<>(data.size());
		for (ItemContainer c : data) {
			c.unadvise();
			oldItems.add(c.item());
		}
		data.clear();
		
		for (int i = 0; i < source.getSize(); ++i) {
			data.add(new ItemContainer(source.getAt(i)));
		}
		Collections.sort(data, makeComparator(order));
		notifyReset(oldItems);
	}
	
	@Override
	protected void onUnlinked() {
		for (ItemContainer c : data) {
			c.unadvise();
		}
	}
	
	private void onItemMutated(T item) {
		// TODO: remove the mutated item and re-insert it at the new position
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
	
	private int indexOfItem(List<ItemContainer> items, T item) {
		// TODO: binary search before scanning for the item.
		
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
	
	private int indexOfFirstGreaterOrEqualItem(T item) {
		//
		// Return index of the first item that is greater or equal than the specified item
		// according to the set order.
		// A new item may be inserted at the returned index.
		//
		return 0;
	}

}
