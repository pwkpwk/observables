package com.ambientbytes.observables;

import java.util.Collection;
import java.util.HashSet;

/**
 * Thread-safe implementation of ITrigger.
 * @author Pavel Karpenko
 *
 */
public final class Trigger implements ITrigger {

	private final IReadWriteMonitor monitor;
	private final Collection<ITriggerListener> listeners;
	
	public Trigger(IReadWriteMonitor monitor) {
		this.monitor = monitor;
		this.listeners = new HashSet<>();
	}

	/**
	 * Call all registered listeners.
	 */
	public void trigger() {
		ITriggerListener[] invocationList = null;
		IResource lock = monitor.acquireRead();
		
		try {
			final int length = listeners.size();
			
			if (length > 0) {
				invocationList = listeners.toArray(new ITriggerListener[length]);
			}
		} finally {
			lock.release();
		}
		
		if (invocationList != null) {
			for (ITriggerListener listener : invocationList) {
				listener.triggered(this);
			}
		}
	}

	@Override
	public void addListener(ITriggerListener listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			if (listener == null || !listeners.add(listener)) {
				throw new IllegalArgumentException("Listener cannot be null and must be unique");
			}
		} finally {
			lock.release();
		}
	}

	@Override
	public void removeListener(ITriggerListener listener) {
		IResource lock = monitor.acquireWrite();
		
		try {
			listeners.remove(listener);
		} finally {
			lock.release();
		}
	}

}
