package com.ambientbytes.observables;

import java.util.Collection;

class MutableObservableList<T> implements IReadOnlyObservableList<T>, ILinked {
	
	private final IReadWriteMonitor monitor;
	private final ArrayListEx<T> data;
	private final ListObservers<T> observers;
	private final IListMutatorListener<T> mutatorListener;
	private IListMutator<T> mutatorCallback;
	
	public MutableObservableList(final IListMutatorListener<T> mutatorListener, final IReadWriteMonitor monitor) {
		if (monitor == null) {
			throw new IllegalArgumentException("monitor cannot be null");
		}
		
		this.monitor = monitor;
		this.data = new ArrayListEx<T>();
		this.observers = new ListObservers<T>(monitor);
		this.mutatorListener = mutatorListener;
		this.mutatorCallback = new IListMutator<T>() {
			
			@Override
			public final void add(T value) {
				IResource res = monitor.acquireWrite();
				
				try {
					insertUnsafe(data.size(), value);
				} finally {
					res.release();
				}
			}

			@Override
			public final void add(int index, T value) {
				IResource res = monitor.acquireWrite();
				
				try {
					insertUnsafe(index, value);
				} finally {
					res.release();
				}
			}
			
			@Override
			public final void add(int index, Collection<T> values) {
				IResource res = monitor.acquireWrite();
				
				try {
					insertUnsafe(index, values);
				} finally {
					res.release();
				}
			}
			
			@Override
			public final void set(int index, T value) {
				IResource res = monitor.acquireWrite();
				
				try {
					setUnsafe(index, value);
				} finally {
					res.release();
				}
			}
			
			@Override
			public final void set(int index, Collection<T> values) {
				IResource res = monitor.acquireWrite();
				
				try {
					setUnsafe(index, values);
				} finally {
					res.release();
				}
			}

			@Override
			public final void remove(int index, int count) {
				IResource res = monitor.acquireWrite();
				
				try {
					removeUnsafe(index, count);
				} finally {
					res.release();
				}
			}

			@Override
			public final void clear() {
				IResource res = monitor.acquireWrite();
				
				try {
					clearUnsafe();
				} finally {
					res.release();
				}
			}

			@Override
			public final void move(int startIndex, int newIndex, int count) {
				IResource res = monitor.acquireWrite();
				
				try {
					moveUnsafe(startIndex, newIndex, count);
				} finally {
					res.release();
				}
			}
			
			@Override
			public final void reset(Collection<T> newItems) {
				IResource res = monitor.acquireWrite();
				
				try {
					resetUnsafe(newItems);
				} finally {
					res.release();
				}
			}
		};
		this.mutatorListener.addListener(mutatorCallback);
	}

	@Override
	public void unlink() {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (mutatorCallback != null) {
				mutatorListener.removeListener(mutatorCallback);
				mutatorCallback = null;
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public final void addObserver(IListObserver observer) {
		observers.add(observer);
	}

	@Override
	public final void removeObserver(IListObserver observer) {
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
			data.remove(index, length);
			observers.removed(index, length);
		}
		
		return length;
	}

	private void insertUnsafe(int index, T value) {
		data.add(index, value);
		observers.added(index, 1);
	}

	private void insertUnsafe(int index, Collection<T> values) {
		if (values.size() != 0) {
			data.addAll(index, values);
			observers.added(index, values.size());
		}
	}
	
	private void setUnsafe(final int index, final T value) {
		if (index < 0 || index >= data.size()) {
			throw new IndexOutOfBoundsException();
		}
		
		observers.changing(index, 1);
		data.set(index, value);
		observers.changed(index, 1);
	}
	
	private void setUnsafe(final int index, final Collection<T> values) {
		final int count = values.size();
		
		if (index < 0 || index + count > data.size()) {
			throw new IndexOutOfBoundsException();
		}
					
		observers.changing(index, count);
		int i = index;
		for (T value : values) {
			data.set(i++, value);
		}
		observers.changed(index, count);
	}

	private void clearUnsafe() {
		final int size = data.size();
		
		if (size > 0) {
			observers.removing(0, size);
			data.clear();
			observers.removed(0, size);
		}
	}

	private void moveUnsafe(int startIndex, int newIndex, int count) {
		data.move(startIndex, newIndex, count);
		
		if (startIndex != newIndex && count > 0) {
			observers.moved(startIndex, newIndex, count);
		}
	}
	
	private void resetUnsafe(Collection<T> newItems) {
		observers.resetting();
		data.clear();
		data.addAll(newItems);
		observers.reset();
	}
}
