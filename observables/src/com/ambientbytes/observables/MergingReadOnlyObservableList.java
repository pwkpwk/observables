package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

final class MergingReadOnlyObservableList<T> implements ILinkedReadOnlyObservableList<T>, IReadOnlyObservableListSet<T> {
	
	private final ReadWriteLock lock;
	private final ListObservers<T> observers;
	private final List<ListInfo> lists;
	private final ArrayListEx<T> data;
	
	private final class ListChange {
		private final int oldSize;
		
		public ListChange(int oldSize) {
			this.oldSize = oldSize;
		}
		
		public int getOldSize() {
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
		
		public ListInfo(IReadOnlyObservableList<T> list, int index, int offset) {
			this.list = list;
			this.list.addObserver(this);
			this.index = index;
			this.offset = offset;
			this.pendingChange = null;
		}
		
		public void unlink() {
			list.removeObserver(this);
		}
		
		public boolean hasList(IReadOnlyObservableList<T> list) {
			return this.list == list;
		}
		
		public int removeData() {
			//
			// Remove all items of the ListInfo from the merged list.
			//
			int length = list.getSize();

			observers.removing(offset, length);
			data.remove(offset, length);
			observers.removed(offset, length);
			
			return length;
		}
		
		public void shiftBack(int indexShift, int itemCount) {
			offset -= itemCount;
			index -= indexShift;
		}
		
		public void shiftForward(int indexShift, int itemCount) {
			offset += itemCount;
			index += indexShift;
		}

		@Override
		public void added(int startIndex, int count) {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				onAddedUnsafe(startIndex, count);
			} finally {
				l.unlock();
			}
		}
		
		@Override
		public void changing(int startIndex, int count) {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				// TODO: implement changing()
			} finally {
				l.unlock();
			}
		}
		
		@Override
		public void changed(int startIndex, int count) {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				// TODO: implement changed()
			} finally {
				l.unlock();
			}
		}

		@Override
		public void removing(int startIndex, int count) {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				onRemovingUnsafe(startIndex, count);
			} finally {
				l.unlock();
			}
		}

		@Override
		public void removed(int startIndex, int count) {
		}

		@Override
		public void moved(int oldStartIndex, int newStartIndex, int count) {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				onMovedUnsafe(oldStartIndex, newStartIndex, count);
			} finally {
				l.unlock();
			}
		}

		@Override
		public void resetting() {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
				pendingChange = new ListChange(list.getSize());
				observers.resetting();
			} finally {
				l.unlock();
			}
		}

		@Override
		public void reset() {
			Lock l = lock.writeLock();

			l.lock();
			
			try {
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
			} finally {
				l.unlock();
			}
		}
		
		private void onAddedUnsafe(int startIndex, int count) {
			if (count > 1) {
				List<T> newItems = new ArrayList<>(count);
				for (int i = 0; i < count; ++i) {
					newItems.add(list.getAt(i));
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
		
		private void onRemovingUnsafe(int startIndex, int count) {
			observers.removing(offset + startIndex, count);
			data.remove(offset + startIndex, count);
			for (int listIndex = index + 1; listIndex < lists.size(); ++listIndex) {
				lists.get(listIndex).shiftBack(0, count);
			}
			observers.removed(offset + startIndex, count);
		}
		
		private void onMovedUnsafe(int oldStartIndex, int newStartIndex, int count) {
			data.move(offset + oldStartIndex, offset + newStartIndex, count);
			observers.moved(offset + oldStartIndex, offset + newStartIndex, count);
		}
	}
	
	public MergingReadOnlyObservableList(ReadWriteLock lock) {
		this.lock = lock;
		this.observers = new ListObservers<>(lock);
		this.lists = new ArrayList<>();
		this.data = new ArrayListEx<>();
	}

	@Override
	public T getAt(int index) {
		T value;
		Lock l = lock.readLock();

		l.lock();
		
		try {
			value = data.get(index);
		} finally {
			l.unlock();
		}
		return value;
	}

	@Override
	public int getSize() {
		int size;
		Lock l = lock.readLock();

		l.lock();
		
		try {
			size = data.size();
		} finally {
			l.unlock();
		}
		return size;
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
	public void add(IReadOnlyObservableList<T> list) {
		Lock l = lock.writeLock();

		l.lock();
		
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
			l.unlock();
		}
	}

	@Override
	public void remove(IReadOnlyObservableList<T> list) {
		Lock l = lock.writeLock();

		l.lock();
		
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
			l.unlock();
		}
	}

	@Override
	public void unlink() {
		Lock l = lock.writeLock();

		l.lock();
		
		try {
			for (ListInfo list : lists) {
				list.unlink();
			}
			lists.clear();
		} finally {
			l.unlock();
		}
	}
}
