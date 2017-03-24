package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

final class ListObserversCollection<T> implements IListObserver<T> {
	
	private final ReadWriteLock lock;
	private final Set<IListObserver<T>> observers;
	
	public ListObserversCollection(final ReadWriteLock lock) {
		this.lock = lock;
		this.observers = new HashSet<IListObserver<T>>();
	}
	
	public void add(IListObserver<T> observer) {
		final Lock l = lock.writeLock();
		
		l.lock();
		
		try {
			if (!observers.add(observer)) {
				throw new IllegalStateException("Duplicate list observer");
			}
		} finally {
			l.unlock();
		}
	}
	
	public void remove(IListObserver<T> observer) {
		final Lock l = lock.writeLock();
		
		l.lock();
		
		try {
			observers.remove(observer);
		} finally {
			l.unlock();
		}
	}

	@Override
	public void added(int startIndex, int count) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.added(startIndex, count);
		}
	}
	
	@Override
	public void removing(int startIndex, int count) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.removing(startIndex, count);
		}
	}

	@Override
	public void removed(int startIndex, int count) {
		for (IListObserver<T> observer : makeInvocationList()) {
			observer.removed(startIndex, count);
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
		Iterable<IListObserver<T>> iterable;
		final Lock l = lock.readLock();
		
		l.lock();
		
		try {
			iterable = new ArrayList<IListObserver<T>>(observers);
		} finally {
			l.unlock();
		}
		
		return iterable;
	}
}
