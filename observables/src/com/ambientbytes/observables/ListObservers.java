package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

final class ListObservers<T> implements IListObserver {
	
	private final ReadWriteLock lock;
	private final Set<IListObserver> observers;
	
	public ListObservers(final ReadWriteLock lock) {
		this.lock = lock;
		this.observers = new HashSet<IListObserver>();
	}
	
	public void add(IListObserver observer) {
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
	
	public void remove(IListObserver observer) {
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
		for (IListObserver observer : makeInvocationList()) {
			observer.added(startIndex, count);
		}
	}
	
	@Override
	public void removing(int startIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.removing(startIndex, count);
		}
	}

	@Override
	public void removed(int startIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.removed(startIndex, count);
		}
	}

	@Override
	public void moved(int oldStartIndex, int newStartIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.moved(oldStartIndex, newStartIndex, count);
		}
	}

	@Override
	public void resetting() {
		for (IListObserver observer : makeInvocationList()) {
			observer.resetting();
		}
	}

	@Override
	public void reset() {
		for (IListObserver observer : makeInvocationList()) {
			observer.reset();
		}
	}
	
	private Iterable<IListObserver> makeInvocationList() {
		Iterable<IListObserver> iterable;
		final Lock l = lock.readLock();
		
		l.lock();
		
		try {
			iterable = new ArrayList<IListObserver>(observers);
		} finally {
			l.unlock();
		}
		
		return iterable;
	}
}
