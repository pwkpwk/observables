package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Concurrency locking helper.
 * @author Pavel Karpenko
 *
 */
public final class LockTool {
    /**
     * Create a new read/write monitor wrapping a Java's ReadWriteLock object.
     * @param lock read/write lock used by the monitor.
     * @return new read/write monitor.
     */
	public static IReadWriteMonitor createReadWriteMonitor(ReadWriteLock lock) {
		return new ReadWriteMonitor(lock);
	}
}
