package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class MergingReadOnlyObservableList<T> implements ILinkedReadOnlyObservableList<T> {
	
	private final IReadWriteMonitor monitor;
	private final ListObservers<T> observers;
	private final List<ListInfo> lists;
	private final ArrayListEx<T> data;
	private final IListSet<T> listSet;
	private final IListSetListener<T> listSetListener;
	
	private final static class ListChange {
		private final int oldSize;
		
		ListChange(int oldSize) {
			this.oldSize = oldSize;
		}
		
		int getOldSize() {
			return oldSize;
		}
	}
	
	/**
	 * Wrapper and observer of dependency observable lists.
	 * Each list added to MergingReadOnlyObservableList is represented by a ListInfo object
	 * that listens to the added list's events and updates the merged collection in MergingReadOnlyObservableList.
	 * List info stores its index in the list of dependency lists maintained by MergingReadOnlyObservableList
	 * and the offset of its first element in the master list (MergingReadOnlyObservableList.data)
	 *
	 */
	private final class ListInfo implements IListObserver {
		private final IReadOnlyObservableList<T> list;
		private int index;	// index of the list in the "lists" collection
		private int offset;	// index of the first element of the list in the "data" collection
		private ListChange pendingChange;
		
		ListInfo(IReadOnlyObservableList<T> list, int index, int offset) {
			this.list = list;
			this.list.addObserver(this);
			this.index = index;
			this.offset = offset;
			this.pendingChange = null;
		}
		
		void unlink() {
			list.removeObserver(this);
		}
		
		boolean hasList(IReadOnlyObservableList<T> list) {
			return this.list == list;
		}
		
		int removeData() {
			//
			// Remove all items of the ListInfo from the merged list.
			//
			int length = list.getSize();

			observers.removing(offset, length);
			data.remove(offset, length);
			observers.removed(offset, length);
			
			return length;
		}
		
		void shiftBack(int indexShift, int itemCount) {
			offset -= itemCount;
			index -= indexShift;
		}
		
		void shiftForward(int indexShift, int itemCount) {
			offset += itemCount;
			index += indexShift;
		}

		@Override
		public void added(int startIndex, int count) {
			if (count > 1) {
				List<T> newItems = new ArrayList<>(count);
				for (int i = 0; i < count; ++i) {
					newItems.add(list.getAt(startIndex + i));
				}
				data.addAll(offset + startIndex, newItems);
			} else {
				data.add(offset + startIndex, list.getAt(startIndex));
			}
			
			for (int listIndex = index + 1; listIndex < lists.size(); ++listIndex) {
				lists.get(listIndex).shiftForward(0, count);
			}
			
			observers.added(offset + startIndex, count);
		}
		
		@Override
		public void changing(int startIndex, int count) {
			observers.changing(offset + startIndex, count);
		}
		
		@Override
		public void changed(int startIndex, int count) {
			for (int i = startIndex; i < startIndex + count; ++i) {
				data.set(offset + i, list.getAt(i));
			}
			observers.changed(offset + startIndex, count);
		}

		@Override
		public void removing(int startIndex, int count) {
			observers.removing(offset + startIndex, count);
			data.remove(offset + startIndex, count);
			for (int listIndex = index + 1; listIndex < lists.size(); ++listIndex) {
				lists.get(listIndex).shiftBack(0, count);
			}
			observers.removed(offset + startIndex, count);
		}

		@Override
		public void removed(int startIndex, int count) {
		}

		@Override
		public void moved(int oldStartIndex, int newStartIndex, int count) {
			data.move(offset + oldStartIndex, offset + newStartIndex, count);
			observers.moved(offset + oldStartIndex, offset + newStartIndex, count);
		}

		@Override
		public void resetting() {
			pendingChange = new ListChange(list.getSize());
			observers.resetting();
		}

		@Override
		public void reset() {
			final int newSize = list.getSize();
			final int sizeDifference = newSize - pendingChange.getOldSize();
			pendingChange = null;
			
			if (sizeDifference <= 0) {
				if (sizeDifference != 0) {
					for (int i = index + 1; i < lists.size(); ++i) {
						lists.get(i).shiftBack(0, -sizeDifference);
					}
					data.remove(offset, -sizeDifference);
				}
				for (int i = 0; i < list.getSize(); ++i) {
					data.set(offset + i, list.getAt(i));
				}
			} else if (sizeDifference > 0) {
				Collection<T> newHeadItems = new ArrayList<>(sizeDifference);
				int i = 0;
				while (i < sizeDifference) {
					newHeadItems.add(list.getAt(i++));
				}
				data.addAll(offset, newHeadItems);
				while (i < newSize) {
					data.set(offset + i, list.getAt(i++));
				}
				for (i = index + 1; i < lists.size(); ++i) {
					lists.get(i).shiftForward(0, sizeDifference);
				}
			}
			observers.reset();
		}
	}
	
	MergingReadOnlyObservableList(IListSet<T> listSet, IReadWriteMonitor monitor) {
		this.monitor = monitor;
		this.observers = new ListObservers<>(monitor);
		this.lists = new ArrayList<>();
		this.data = new ArrayListEx<>();
		this.listSet = listSet;
		
		for (IReadOnlyObservableList<T> list : listSet) {
			add(list);
		}
		this.listSetListener = new IListSetListener<T>() {

			@Override
			public void added(IListSet<T> source, IReadOnlyObservableList<T> list) {
				add(list);
			}

			@Override
			public void removed(IListSet<T> source, IReadOnlyObservableList<T> list) {
				remove(list);
			}
			
		};
		this.listSet.addListener(this.listSetListener);
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
	public void addObserver(IListObserver observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(IListObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void unlink() {
		IResource res = monitor.acquireWrite();
		
		try {
			listSet.removeListener(listSetListener);
			for (ListInfo list : lists) {
				list.unlink();
			}
			lists.clear();
		} finally {
			res.release();
		}
	}

	private void add(IReadOnlyObservableList<T> list) {
		IResource res = monitor.acquireWrite();
		
		try {
			for (ListInfo listInfo : lists) {
				if (listInfo.hasList(list)) {
					throw new IllegalArgumentException("duplicate list in the collection");
				}
			}
			
			int startIndex = data.size();
			int length = list.getSize();
			
			lists.add(new ListInfo(list, lists.size(), startIndex));
			
			if (length > 0) {
				data.ensureCapacity(data.size() + length);
				for (int i = 0; i < length; ++i) {
					data.add(list.getAt(i));
				}
				observers.added(startIndex, length);
			}
		} finally {
			res.release();
		}
	}

	private void remove(IReadOnlyObservableList<T> list) {
		IResource res = monitor.acquireWrite();
		
		try {
			int i = 0;
			
			while(i < lists.size()) {
				ListInfo listInfo = lists.get(i);
				
				if (listInfo.hasList(list)) {
					listInfo.unlink();
					int removedLength = listInfo.removeData();
					lists.remove(i);
					
					while (i < lists.size()) {
						listInfo = lists.get(i);
						listInfo.shiftBack(1, removedLength);
						++i;
					}
				} else {
					++i;
				}
			}
		} finally {
			res.release();
		}
	}
}
