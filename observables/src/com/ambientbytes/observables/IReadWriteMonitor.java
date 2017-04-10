package com.ambientbytes.observables;

/**
 * Read/write monitor.
 * @author Pavel Karpenko
 */

public interface IReadWriteMonitor {
    /**
     * Acquire a read lock on the monitor and return a resource that releases the lock.
     * @return resource object that releases the acquired lock.
     */
    IResource acquireRead();

    /**
     * Acquire a write lock on the monitor and return a resource that releases the lock.
     * @return resource object that releases the acquired lock.
     */
    IResource acquireWrite();
}
