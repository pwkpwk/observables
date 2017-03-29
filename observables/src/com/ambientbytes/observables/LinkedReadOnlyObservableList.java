package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Base class for observable lists that observe changes in one other observable list.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in the list.
 */
abstract class LinkedReadOnlyObservableList<T> implements ILinkedReadOnlyObservableList<T> {

	private final ListObservers<T> observers;
	private final ReadWriteLock lock;
	private IReadOnlyObservableList<T> source;
	private IListObserver observer;

	private final class ListObserver implements IListObserver {

		private final IReadOnlyObservableList<T> source;

		public ListObserver(IReadOnlyObservableList<T> source) {
			this.source = source;
		}

		@Override
		public void added(int startIndex, int count) {
            onAdded(source, startIndex, count);
		}
        @Override
        public void changing(int startIndex, int count) {
            onChanging(source, startIndex, count);
        }

        @Override
        public void changed(int startIndex, int count) {
            onChanged(source, startIndex, count);
        }

        @Override
        public void removing(int startIndex, int count) {
            onRemoving(source, startIndex, count);
        }

        @Override
        public void removed(int startIndex, int count) {
            onRemoved(source, startIndex, count);
        }

        @Override
        public void moved(int oldStartIndex, int newStartIndex, int count) {
            onMoved(source, oldStartIndex, newStartIndex, count);
        }

        @Override
        public void resetting() {
            onResetting(source);
        }

        @Override
        public void reset() {
            onReset(source);
        }
	}
	
	protected LinkedReadOnlyObservableList(IReadOnlyObservableList<T> source, ReadWriteLock lock) {
		this.observers = new ListObservers<T>(lock);
		this.lock = lock;
		this.source = source;
		this.observer = new ListObserver(source);
		source.addObserver(observer);
	}

	@Override
	public final void addObserver(IListObserver observer) {
		this.observers.add(observer);
	}

	@Override
	public final void removeObserver(IListObserver observer) {
		this.observers.remove(observer);
	}

	@Override
	public final void unlink() {
		if (source != null) {
			source.removeObserver(observer);
			source = null;
			observer = null;
			onUnlinked();
		}
	}

	protected void onUnlinked() {}
	protected abstract void onAdded(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onChanging(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onChanged(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onRemoving(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onRemoved(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count);
	protected abstract void onResetting(IReadOnlyObservableList<T> source);
	protected abstract void onReset(IReadOnlyObservableList<T> source);

	protected ReadWriteLock lock() {
		return lock;
	}
	
	protected final void notifyAdded(int startIndex, int count) {
		this.observers.added(startIndex, count);
	}
	
	protected final void notifyChanging(int startIndex, int count) {
		this.observers.changing(startIndex, count);
	}
	
	protected final void notifyChanged(int startIndex, int count) {
		this.observers.changed(startIndex, count);
	}
	
	protected final void notifyRemoving(int startIndex, int count) {
		this.observers.removing(startIndex, count);
	}
	
	protected final void notifyRemoved(int startIndex, int count) {
		this.observers.removed(startIndex, count);
	}

	protected final void notifyMoved(int oldStartIndex, int newStartIndex, int count) {
		this.observers.moved(oldStartIndex, newStartIndex, count);
	}
	
	protected final void notifyResetting() {
		this.observers.resetting();
	}
	
	protected final void notifyReset() {
		this.observers.reset();
	}
}
