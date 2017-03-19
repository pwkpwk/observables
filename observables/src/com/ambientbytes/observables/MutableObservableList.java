package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

class MutableObservableList<T> implements IReadOnlyObservableList<T> {
	
	private final ReadWriteLock lock;
	private final List<T> data;
	private final ListObserversCollection<T> observers;
	private final IListMutator<T> mutator;
	
	private final class Mutator implements IListMutator<T> {

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
		public final void remove(int index, int count) {
			final Lock l = lock.writeLock();
			
			l.lock();
			
			try {
				removeUnsafe(index, count);
			} finally {
				l.unlock();
			}
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

		private void removeUnsafe(int index, int count) {
			if (index < 0 || index >= data.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			int length = count;
			
			if (index + length > data.size()) {
				length -= data.size() - index;
			}
			
			if (length > 0) {
				ArrayList<T> removedValues = new ArrayList<T>(length);
				for (int i = 0; i < length; ++i) {
					removedValues.add(data.get(index));
					data.remove(index);
				}
				
				observers.removed(index, removedValues);
			}
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
			if (startIndex < 0 || newIndex < 0 || startIndex >= data.size() || newIndex >= data.size()) {
				throw new IndexOutOfBoundsException();
			}

			if (startIndex != newIndex) {
				int length = count;
				
				if (startIndex + length > data.size()) {
					length -= data.size() - startIndex;
				}
				
				if (length > 0) {
					if (newIndex + length >= data.size()) {
						throw new IndexOutOfBoundsException();
					}
					
					//
					// Preserve the source range
					//
					List<T> source = new ArrayList<>(length);
					for (int i = 0; i < length; ++i) {
						source.add(data.get(startIndex + i));
					}

					if (startIndex < newIndex) {
						// Move the items up.
						final int shiftedLength = newIndex - startIndex;
						//
						// Fill the gap left by the removed source range with elements immediately after it.
						//
						for (int i = 0; i < shiftedLength; ++i) {
							data.set(startIndex + i, data.get(startIndex + length + i));
						}
						//
						// Put the preserved source range to its new place.
						//
						for (int i = 0; i < length; ++i) {
							data.set(newIndex + i, source.get(i));
						}
					} else {
						// Move the items down.
						final int shiftedLength = startIndex - newIndex;
						
						for (int i = 0; i < shiftedLength; ++i) {
							data.set(startIndex + length - 1 - i, data.get(newIndex + shiftedLength - 1 - i));
						}
						//
						// Put the preserved source range to its new place.
						//
						for (int i = 0; i < length; ++i) {
							data.set(newIndex + i, source.get(i));
						}
					}
					observers.moved(startIndex, newIndex, length);
				}
			}
		}
		
	}
	
	public MutableObservableList(final ReadWriteLock lock) {
		if (lock == null) {
			throw new IllegalArgumentException("lock cannot be null");
		}
		
		this.lock = lock;
		this.data = new ArrayList<T>();
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
