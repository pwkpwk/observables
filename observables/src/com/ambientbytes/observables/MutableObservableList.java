package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

class MutableObservableList<T> implements IReadOnlyObservableList<T> {
	
	private final ReadWriteLock lock;
	private final ArrayListEx<T> data;
	private final ListObserversCollection<T> observers;
	private final IListMutator<T> mutator;
	
	private final class Mutator implements IListMutator<T> {
		
		@Override
		public final void add(T value) {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				insertUnsafe(data.size(), value);
			} finally {
				l.unlock();
			}
		}

		@Override
		public final void insert(int index, T value) {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				insertUnsafe(index, value);
			} finally {
				l.unlock();
			}
		}

		@Override
		public final int remove(int index, int count) {
			final Lock l = lock.writeLock();
			int length;
			
			l.lock();
			
			try {
				length = removeUnsafe(index, count);
			} finally {
				l.unlock();
			}
			
			return length;
		}

		@Override
		public final void clear() {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				clearUnsafe();
			} finally {
				l.unlock();
			}
		}

		@Override
		public final void move(int startIndex, int newIndex, int count) {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				moveUnsafe(startIndex, newIndex, count);
			} finally {
				l.unlock();
			}
		}
		
		@Override
		public final void reset(Collection<T> newItems) {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				resetUnsafe(newItems);
			} finally {
				l.unlock();
			}
		}

		private int removeUnsafe(int index, int count) {
			if (index < 0 || index >= data.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			int length = count;
			
			if (index + length > data.size()) {
				length = data.size() - index;
			}
			
			if (length > 0) {
				observers.removing(index, length);
				ArrayList<T> removedValues = new ArrayList<T>(length);
				for (int i = 0; i < length; ++i) {
					removedValues.add(data.get(index));
				}
				data.remove(index, length);
				
				observers.removed(index, removedValues);
			}
			
			return length;
		}

		private void insertUnsafe(int index, T value) {
			data.add(index, value);
			observers.added(index, 1);
		}

		private void clearUnsafe() {
			if (data.size() > 0) {
				Collection<T> oldData = new ArrayList<T>(data);
				data.clear();
				observers.removed(0, oldData);
			}
		}

		private void moveUnsafe(int startIndex, int newIndex, int count) {
			data.move(startIndex, newIndex, count);
			
			if (count > 0) {
				observers.moved(startIndex, newIndex, count);
			}
		}
		
		private void resetUnsafe(Collection<T> newItems) {
			List<T> oldItems = new ArrayList<>(data);
			
			data.clear();
			data.addAll(newItems);
			
			observers.reset(oldItems);
		}
	}
	
	public MutableObservableList(final ReadWriteLock lock) {
		if (lock == null) {
			throw new IllegalArgumentException("lock cannot be null");
		}
		
		this.lock = lock;
		this.data = new ArrayListEx<T>();
		this.observers = new ListObserversCollection<T>(lock);
		this.mutator = new Mutator();
	}
	
	public final IListMutator<T> getMutator() {
		return mutator;
	}

	@Override
	public final void addObserver(IListObserver<T> observer) {
		observers.add(observer);
	}

	@Override
	public final void removeObserver(IListObserver<T> observer) {
		observers.remove(observer);
	}

	@Override
	public final T getAt(int index) {
		return data.get(index);
	}

	@Override
	public final int getSize() {
		return data.size();
	}
}
