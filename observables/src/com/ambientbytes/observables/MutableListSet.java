package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Mutable implementation of IListSet that acquires a write lock on a read/write monitor before any mutations.
 * @author Pavel Karpenko
 *
 * @param <T>
 */
public final class MutableListSet<T> implements IListSet<T> {

	private final IReadWriteMonitor monitor;
	private final Collection<IReadOnlyObservableList<T>> lists;
	private final Collection<IListSetListener<T>> listeners;
	
	public MutableListSet(IReadWriteMonitor monitor) {
		this.monitor = monitor;
		this.lists = new HashSet<>();
		this.listeners = new HashSet<>();
		
		for (IReadOnlyObservableList<T> list : lists) {
			if (!this.lists.add(list)) {
				throw new IllegalStateException("Cannot add a list more that once.");
			}
		}
	}
	
	@SafeVarargs
	public MutableListSet(IReadWriteMonitor monitor, IReadOnlyObservableList<T>... lists) {
		this(monitor);
		
		for (IReadOnlyObservableList<T> list : lists) {
			if (!this.lists.add(list)) {
				throw new IllegalArgumentException("Cannot add a list more that once.");
			}
		}
	}
	
	public final void add(IReadOnlyObservableList<T> list) {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (lists.add(list)) {
				for (IListSetListener<T> listener : listeners) {
					listener.added(this, list);
				}
			} else {
				throw new IllegalArgumentException("Cannot add a list more that once.");
			}
		} finally {
			lock.release();
		}
	}
	
	public final void remove(IReadOnlyObservableList<T> list) {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (lists.remove(list)) {
				for (IListSetListener<T> listener : listeners) {
					listener.removed(this, list);
				}
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public Iterator<IReadOnlyObservableList<T>> iterator() {
		Collection<IReadOnlyObservableList<T>> copy;
		IResource lock = monitor.acquireRead();
		
		try {
			copy = new ArrayList<>(lists);
		} finally {
			lock.release();
		}
		
		return copy.iterator();
	}

	@Override
	public void addListener(IListSetListener<T> listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (!listeners.add(listener)) {
				throw new IllegalArgumentException("Cannot add a listener more that once.");
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void removeListener(IListSetListener<T> listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			listeners.remove(listener);
		} finally {
			lock.release();
		}
	}

}
