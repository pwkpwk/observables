package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

final class ListObservers<T> implements IListObserver {
	
	private final IReadWriteMonitor monitor;
	private final Set<IListObserver> observers;
	
	ListObservers(final IReadWriteMonitor monitor) {
		this.monitor = monitor;
		this.observers = new HashSet<>();
	}
	
	public void add(IListObserver observer) {
		final IResource l = monitor.acquireWrite();
		
		try {
			if (!observers.add(observer)) {
				throw new IllegalStateException("Duplicate list observer");
			}
		} finally {
			l.release();
		}
	}
	
	public void remove(IListObserver observer) {
		final IResource l = monitor.acquireWrite();
		
		try {
			observers.remove(observer);
		} finally {
			l.release();
		}
	}

	@Override
	public void added(int startIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.added(startIndex, count);
		}
	}
	
	@Override
	public void changing(int startIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.changing(startIndex, count);
		}
	}
	
	@Override
	public void changed(int startIndex, int count) {
		for (IListObserver observer : makeInvocationList()) {
			observer.changed(startIndex, count);
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
		final IResource l = monitor.acquireRead();
		
		try {
			iterable = new ArrayList<>(observers);
		} finally {
			l.release();
		}
		
		return iterable;
	}
}
