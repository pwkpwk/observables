package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class FilteringReadOnlyObservableList<T>
		extends LinkedReadOnlyObservableList<T>
		implements IItemFilterContainer<T> {
	
	private final List<ItemContainer> data;
	private final Map<T, ItemContainer> filteredOutItems;
	private IItemFilter<T> filter;
	
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
			if (mutable != null) {
				mutable.removeObserver(this);
			}
		}

		@Override
		public final void mutated() {
			onItemMutated(item);
		}
	}

	public FilteringReadOnlyObservableList(
			IReadOnlyObservableList<T> source,
			IItemFilter<T> filter) {
		super(source);
		this.data = new ArrayList<ItemContainer>(source.getSize());
		this.filteredOutItems = new HashMap<T, ItemContainer>();
		this.filter = filter;
		
		int size = source.getSize();
		
		for (int i = 0; i < size; ++i) {
			addItem(source.getAt(i));
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
	protected void onUnlinked() {
		removeMutableObserverFromItems(data);
		removeMutableObserverFromItems(filteredOutItems.values());
		filteredOutItems.clear();
	}
	
	@Override
	protected void onAdded(IReadOnlyObservableList<T> source, int startIndex, int count) {
		int reportedStartIndex = data.size();
		int reportedCount = 0;
		
		for (int i = 0; i < count; ++i) {
			if (addItem(source.getAt(startIndex + i))) {
				++reportedCount;
			}
		}
		
		if (reportedCount > 0) {
			notifyAdded(reportedStartIndex, reportedCount);
		}
	}

	@Override
	protected void onRemoving(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		for (int i = startIndex; i < startIndex + count; ++i) {
			final T removedItem = source.getAt(i);
			ItemContainer container = filteredOutItems.remove(removedItem);
			
			if (container != null) {
				// No need to notify observers; the item was not visible to them.
				container.unadvise();
			} else {
				int index = indexOfContainer(removedItem);
				
				if (index >= 0) {
					data.get(index).unadvise();
					notifyRemoving(index, 1);
					data.remove(index);
					notifyRemoved(index, 1);
				}
			}
		}
	}
	
	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, int count) {
		// Do nothing. Items have been removed in onRemoving.
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		// Do nothing. Moving items in the source collection does not affect filtering.
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source, Collection<T> items) {
		Collection<T> oldItems = new ArrayList<T>(data.size());
		for (ItemContainer c : data) {
			oldItems.add(c.item());
		}
		removeMutableObserverFromItems(data);
		removeMutableObserverFromItems(filteredOutItems.values());
		data.clear();
		filteredOutItems.clear();

		int size = source.getSize();
		
		for (int i = 0; i < size; ++i) {
			addItem(source.getAt(i));
		}
		
		notifyReset(oldItems);
	}

	@Override
	public IItemFilter<T> getFilter() {
		return filter;
	}

	@Override
	public void setFilter(IItemFilter<T> filter) {
		if (this.filter != filter) {
			this.filter = filter;
			
			Collection<T> oldItems = new ArrayList<T>(data.size());
			Collection<ItemContainer> allItems = new ArrayList<ItemContainer>(data.size() + filteredOutItems.size());
			
			for (ItemContainer c : data) {
				oldItems.add(c.item());
			}
			
			allItems.addAll(data);
			allItems.addAll(filteredOutItems.values());
			data.clear();
			filteredOutItems.clear();
			
			for (ItemContainer c : allItems) {
				if (filter.isIn(c.item())) {
					data.add(c);
				} else {
					filteredOutItems.put(c.item(), c);
				}
			}
			
			notifyReset(oldItems);
		}
	}
	
	private void onItemMutated(T item) {
		if (filter.isIn(item)) {
			ItemContainer container = filteredOutItems.remove(item);
			
			if (container != null) {
				int index = this.data.size();
				this.data.add(container);
				notifyAdded(index, 1);
			}
		} else {
			final int index = indexOfContainer(item);
			
			if (index >= 0) {
				filteredOutItems.put(item, data.get(index));
				notifyRemoving(index, 1);
				data.remove(index);
				notifyRemoved(index, 1);
			}
		}
	}
	
	private void removeMutableObserverFromItems(Collection<ItemContainer> containers) {
		for (ItemContainer container : containers) {
			container.unadvise();
		}
	}
	
	private boolean addItem(T item) {
		final boolean added = filter.isIn(item);
		
		ItemContainer container = new ItemContainer(item);
		
		if (added) {
			this.data.add(container);
		} else {
			this.filteredOutItems.put(item, container);
		}
		
		return added;
	}
	
	private int indexOfContainer(T item) {
		final int size = data.size();
		int index = -1;

		for (int i = 0; i < size && index < 0;) {
			if (data.get(i).item() == item) {
				index = i;
			} else {
				++i;
			}
		}
		
		return index;
	}
}
