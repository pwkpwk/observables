package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FilteringReadOnlyObservableList<T>
		extends LinkedReadOnlyObservableList<T>
		implements IItemFilterContainer<T> {
	
	private final List<T> data;
	private final Set<T> filteredOutItems;
	private final IObjectMutationObserver mutationObserver;
	private IItemFilter<T> filter;

	protected FilteringReadOnlyObservableList(
			IReadOnlyObservableList<T> source,
			IItemFilter<T> filter) {
		super(source);
		this.data = new ArrayList<T>(source.getSize());
		this.filteredOutItems = new HashSet<T>();
		this.mutationObserver = new IObjectMutationObserver() {
			@SuppressWarnings("unchecked")
			@Override
			public void mutated(IMutableObject source) {
				onItemMutated((T)source);
			}
		};
		this.filter = filter;
		
		int size = source.getSize();
		
		for (int i = 0; i < size; ++i) {
			addItem(source.getAt(i));
		}
	}

	@Override
	public T getAt(int index) {
		return data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	protected void onUnlinked() {
		removeMutableObserverFromItems(data);
		removeMutableObserverFromItems(filteredOutItems);
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
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, Collection<T> items) {
		
		removeMutableObserverFromItems(items);
		
		for (T item : items) {
			if (!filteredOutItems.remove(item)) {
				int index = data.indexOf(item);
				
				if (index >= 0) {
					final List<T> removedItem = new ArrayList<T>(1);
					removedItem.add(item);
					data.remove(index);
					notifyRemoved(index, removedItem);
				}
			}
		}
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		// Do nothing. Moving items in the source collection does not affect filtering.
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source, Collection<T> items) {
		Collection<T> oldItems = new ArrayList<T>(data);
		removeMutableObserverFromItems(data);
		removeMutableObserverFromItems(filteredOutItems);
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
			
			Collection<T> oldItems = new ArrayList<T>(data);
			Collection<T> allItems = new ArrayList<T>(data.size() + filteredOutItems.size());
			
			allItems.addAll(data);
			allItems.addAll(filteredOutItems);
			data.clear();
			filteredOutItems.clear();
			
			for (T item : allItems) {
				if (filter.isIn(item)) {
					data.add(item);
				} else {
					filteredOutItems.add(item);
				}
			}
			
			notifyReset(oldItems);
		}
	}
	
	private void onItemMutated(T item) {
		if (filter.isIn(item)) {
			if (filteredOutItems.remove(item)) {
				int index = this.data.size();
				this.data.add(item);
				notifyAdded(index, 1);
			}
		} else {
			int index = this.data.indexOf(item);
			
			if (index >= 0) {
				Collection<T> removedItems = new ArrayList<T>(1);
				removedItems.add(item);
				data.remove(index);
				filteredOutItems.add(item);
				notifyRemoved(index, removedItems);
			}
		}
	}
	
	private void removeMutableObserverFromItems(Collection<T> items) {
		for (T item : items) {
			if (item instanceof IMutableObject) {
				IMutableObject mutable = (IMutableObject) item;
				mutable.removeObserver(mutationObserver);
			}
		}
	}
	
	private boolean addItem(T item) {
		final boolean added = filter.isIn(item);
		
		if (item instanceof IMutableObject) {
			IMutableObject mutable = (IMutableObject) item;
			mutable.addObserver(mutationObserver);
		}
		
		if (added) {
			this.data.add(item);
		} else {
			this.filteredOutItems.add(item);
		}
		
		return added;
	}
}
