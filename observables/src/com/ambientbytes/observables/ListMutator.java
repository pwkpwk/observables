package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public final class ListMutator<T> implements IListMutator<T>, IListMutatorListener<T> {

	private final IReadWriteMonitor monitor;
	private final Collection<IListMutator<T>> listeners;
	
	public ListMutator(IReadWriteMonitor monitor) {
		this.monitor = monitor;
		this.listeners = new HashSet<>();
	}

	@Override
	public void addListener(IListMutator<T> listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (listener == null || !listeners.add(listener)) {
				throw new IllegalArgumentException("Listeners must be unique and non-null.");
			}
		} finally {
			lock.release();
		}		
	}

	@Override
	public void removeListener(IListMutator<T> listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			listeners.remove(listener);
		} finally {
			lock.release();
		}
	}

	@Override
	public void add(T value) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.add(value);
		}
	}

	@Override
	public void add(int index, T value) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.add(index, value);
		}
	}

	@Override
	public void add(int index, Collection<T> values) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.add(index, values);
		}
	}

	@Override
	public void set(int index, T value) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.set(index, value);
		}
	}

	@Override
	public void set(int index, Collection<T> values) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.set(index, values);
		}
	}

	@Override
	public void remove(int index, int count) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.remove(index, count);
		}
	}

	@Override
	public void clear() {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.clear();
		}
	}

	@Override
	public void move(int startIndex, int newIndex, int count) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.move(startIndex, newIndex, count);
		}
	}

	@Override
	public void reset(Collection<T> newItems) {
		for (IListMutator<T> mutator : makeInvocationList()) {
			mutator.reset(newItems);
		}
	}
	
	private Iterable<IListMutator<T>> makeInvocationList() {
		IResource lock = monitor.acquireRead();
		Iterable<IListMutator<T>> iterable;
		
		try {
			iterable = new ArrayList<>(listeners);
		} finally {
			lock.release();
		}
		
		return iterable;
	}

}
