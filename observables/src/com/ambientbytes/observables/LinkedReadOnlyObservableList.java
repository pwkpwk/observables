package com.ambientbytes.observables;

import java.util.Collection;

abstract class LinkedReadOnlyObservableList<T> implements ILinkedReadOnlyObservableList<T> {

	private final ListObserversCollection<T> observers;
	private IReadOnlyObservableList<T> source;
	private IListObserver<T> observer;
	
	protected LinkedReadOnlyObservableList(IReadOnlyObservableList<T> source) {
		this.observers = new ListObserversCollection<T>(new DummyReadWriteLock());
		this.source = source;
		this.observer = new IListObserver<T>() {

			@Override
			public void added(int startIndex, int count) {
				onAdded(source, startIndex, count);
			}

			@Override
			public void removed(int startIndex, Collection<T> items) {
				onRemoved(source, startIndex, items);
			}

			@Override
			public void moved(int oldStartIndex, int newStartIndex, int count) {
				onMoved(source, oldStartIndex, newStartIndex, count);
			}

			@Override
			public void reset(Collection<T> oldItems) {
				onReset(source, oldItems);
			}
			
		};
		source.addObserver(observer);
	}

	@Override
	public final void addObserver(IListObserver<T> observer) {
		this.observers.add(observer);
	}

	@Override
	public final void removeObserver(IListObserver<T> observer) {
		this.observers.remove(observer);
	}

	@Override
	public final void unlink() {
		if (source != null) {
			source.removeObserver(observer);
			source = null;
			observer = null;
		}
	}
	
	protected abstract void onAdded(IReadOnlyObservableList<T> source, int startIndex, int count);
	protected abstract void onRemoved(IReadOnlyObservableList<T> source, int startIndex, Collection<T> items);
	protected abstract void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count);
	protected abstract void onReset(IReadOnlyObservableList<T> source, Collection<T> items);
	
	protected final void notifyAdded(int startIndex, int count) {
		this.observers.added(startIndex, count);
	}
	
	protected final void notifyRemoved(int startIndex, Collection<T> items) {
		this.observers.removed(startIndex, items);
	}

	protected final void notifyMoved(int oldStartIndex, int newStartIndex, int count) {
		this.observers.moved(oldStartIndex, newStartIndex, count);
	}
	
	protected final void notifyReset(Collection<T> oldItems) {
		this.observers.reset(oldItems);
	}
}
