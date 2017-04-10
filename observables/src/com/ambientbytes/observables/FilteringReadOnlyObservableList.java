package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class FilteringReadOnlyObservableList<T>
		extends LinkedReadOnlyObservableList<T>
		implements IItemFilterContainer<T> {
	
	private final ArrayListEx<ItemContainer> data;
	private final Map<T, ItemContainer> filteredOutItems;
	private IItemFilter<T> filter;
	private Set<Integer> pendingChange;
	
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
			IItemFilter<T> filter,
			IReadWriteMonitor monitor) {
		super(source, monitor);
		this.data = new ArrayListEx<ItemContainer>(source.getSize());
		this.filteredOutItems = new HashMap<T, ItemContainer>();
		this.filter = filter;
		this.pendingChange = null;
		
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
	protected void onChanging(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		//
		// Create an ordered set of indexes so later changed items may be bundled up
		// into ranges for reporting to observers.
		//
		pendingChange = new TreeSet<>();
		
		for (int i = startIndex; i < startIndex + count; ++i) {
			final T changingItem = source.getAt(i);
			final ItemContainer container = filteredOutItems.remove(changingItem);
			
			if (container != null) {
				container.unadvise();
			} else {
				int index = indexOfContainer(changingItem);
				
				data.get(index).unadvise();
				//
				// Remember position of the changing item so it can be replaced with a new one later.
				//
				pendingChange.add(index);
			}
		}
	}
	
	@Override
	protected void onChanged(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		Collection<ItemContainer> backItems = null;
		int backItemsCapacity = pendingChange.size();
		Iterator<Integer> emptySlots = pendingChange.iterator();
		
		for (int i = startIndex; i < startIndex + count; ++i) {
			final ItemContainer container = new ItemContainer(source.getAt(i));
			
			if (filter.isIn(container.item())) {
				if (emptySlots.hasNext()) {
					final int index = emptySlots.next().intValue();
					notifyChanging(index, 1);
					data.set(index, container);
					// TODO: optimize reporting - build ranges if added items are adjacent.
					notifyChanged(index, 1);
					backItemsCapacity--;
				} else {
					//
					// Ran out of empty slots, add items at the back of the list
					//
					if (backItems == null) {
						backItems = new ArrayList<>(backItemsCapacity);
					}
					backItems.add(container);
				}
			} else {
				filteredOutItems.put(container.item(), container);
			}
		}
		
		if (backItems != null) {
			//
			// Items must be added at the back of the list.
			//
			final int index = data.size();
			data.addAll(backItems);
			notifyAdded(index, backItems.size());
		} else {
			//
			// There may be more empty slots - remove them.
			// The iterator will give indexes in the ascending order so each removal
			// will shift the list back.
			//
			int shift = 0; // the accumulated index shift after removal of all items so far.
			RangeDetector rd = new RangeDetector();
			RangeDetector.Range range = null;
			
			while (emptySlots.hasNext()) {
				final int index = emptySlots.next().intValue() - shift;
				range = rd.addIndex(index);
				
				if (range != null) {
					for (int i = range.start(); i < range.start() + range.length(); ++i) {
						data.get(i).unadvise();
					}
					notifyRemoving(range.start(), range.length());
					data.remove(range.start(), range.length());
					shift += range.length();
					notifyRemoved(range.start(), range.length());
				}
			}

			range = rd.finish();
			
			if (range != null) {
				for (int i = range.start(); i < range.start() + range.length(); ++i) {
					data.get(i).unadvise();
				}
				notifyRemoving(range.start(), range.length());
				data.remove(range.start(), range.length());
				notifyRemoved(range.start(), range.length());
			}
		}
		
		pendingChange = null;
	}

	@Override
	protected void onRemoving(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		Set<Integer> removedIndexes = new TreeSet<>();
		
		for (int i = startIndex; i < startIndex + count; ++i) {
			final T removedItem = source.getAt(i);
			ItemContainer container = filteredOutItems.remove(removedItem);
			
			if (container != null) {
				// No need to notify observers; the item was not visible to them.
				container.unadvise();
			} else {
				int index = indexOfContainer(removedItem);
				
				if (index >= 0) {
					removedIndexes.add(index);
				}
			}
		}
		
		if (!removedIndexes.isEmpty()) {
			RangeDetector rd = new RangeDetector();
			RangeDetector.Range range = null;
			int shift = 0;
			
			for (Integer index : removedIndexes) {
				range = rd.addIndex(index.intValue() - shift);
				
				if (range != null) {
					for (int i = range.start(); i < range.start() + range.length(); ++i) {
						data.get(i).unadvise();
					}
					notifyRemoving(range.start(), range.length());
					data.remove(range.start(), range.length());
					shift += range.length();
					notifyRemoved(range.start(), range.length());
				}
			}
			
			range = rd.finish();

			if (range != null) {
				for (int i = range.start(); i < range.start() + range.length(); ++i) {
					data.get(i).unadvise();
				}
				notifyRemoving(range.start(), range.length());
				data.remove(range.start(), range.length());
				notifyRemoved(range.start(), range.length());
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
	protected void onResetting(IReadOnlyObservableList<T> source) {
		notifyResetting();
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source) {
		removeMutableObserverFromItems(data);
		removeMutableObserverFromItems(filteredOutItems.values());		
		data.clear();
		filteredOutItems.clear();

		int size = source.getSize();
		
		for (int i = 0; i < size; ++i) {
			addItem(source.getAt(i));
		}
		
		notifyReset();
	}

	@Override
	public IItemFilter<T> getFilter() {
		return filter;
	}

	@Override
	public void setFilter(IItemFilter<T> filter) {
		if (this.filter != filter) {
			Collection<ItemContainer> allItems = new ArrayList<ItemContainer>(data.size() + filteredOutItems.size());
			
			this.filter = filter;
			notifyResetting();
			
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
			
			notifyReset();
		}
	}
	
	private void onItemMutated(T item) {
		//
		// Item mutations must be processed under a write lock because they
		// may change the collection that is updated by event handlers that are supposed
		// to be synchronized by the same lock (all collections in the pipeline are supposed
		// to share a single lock).
		//
		IResource res = monitor().acquireWrite();
		
		try {
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
		} finally {
			res.release();
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
