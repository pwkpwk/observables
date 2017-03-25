package com.ambientbytes.observables;

/**
 * Base class for observable lists that observe changes in one other observable list.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in the list.
 */
abstract class LinkedReadOnlyObservableList<T> implements ILinkedReadOnlyObservableList<T> {

	private final ListObserversCollection<T> observers;
	private IReadOnlyObservableList<T> source;
	private IListObserver observer;
	
	protected LinkedReadOnlyObservableList(IReadOnlyObservableList<T> source) {
		this.observers = new ListObserversCollection<T>(new DummyReadWriteLock());
		this.source = source;
		this.observer = new IListObserver() {

			@Override
			public void added(int startIndex, int count) {
				onAdded(source, startIndex, count);
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
			
		};
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
	protected abstract void onRemoving(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onRemoved(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count);
	protected abstract void onResetting(IReadOnlyObservableList<T> source);
	protected abstract void onReset(IReadOnlyObservableList<T> source);
	
	protected final void notifyAdded(int startIndex, int count) {
		this.observers.added(startIndex, count);
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
