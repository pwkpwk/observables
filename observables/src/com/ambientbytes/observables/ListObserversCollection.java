package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class ListObserversCollection<T> implements IListObserver<T> {
	private final Set<IListObserver<T>> observers;
	
	public ListObserversCollection() {
		observers = new HashSet<IListObserver<T>>();
	}
	
	public void add(IListObserver<T> observer) {
		if (!observers.add(observer)) {
			throw new IllegalStateException("Duplicate list observer");
		}
	}
	
	public void remove(IListObserver<T> observer) {
		observers.remove(observer);
	}

	@Override
	public void added(int startIndex, int count) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.added(startIndex, count);
		}
	}

	@Override
	public void removed(int startIndex, Collection<T> items) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.removed(startIndex, items);
		}
	}

	@Override
	public void moved(int oldStartIndex, int newStartIndex, int count) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.moved(oldStartIndex, newStartIndex, count);
		}
	}

	@Override
	public void reset(Collection<T> oldItems) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.reset(oldItems);
		}
	}
	
	private Iterable<IListObserver<T>> makeInvocationList() {
		return new ArrayList<IListObserver<T>>(observers);
	}
}
