package com.ambientbytes.observables;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Implementation of IReadWriteMonitor that uses Java's ReadWriteLock interface to acquire and release locks.
 */

final class ReadWriteMonitor implements IReadWriteMonitor {

    private final ReadWriteLock lock;

    private static final class LockResource implements IResource {
        private final AtomicReference<Lock> lock;

        LockResource(Lock lock) {
            lock.lock();
            this.lock = new AtomicReference<>(lock);
        }


        @Override
        public void release() {
            final Lock oldLock = this.lock.getAndSet(null);

            if (oldLock != null) {
                oldLock.unlock();
            }
        }
    }

    public ReadWriteMonitor(ReadWriteLock lock) {
        this.lock = lock;
    }

    @Override
    public IResource acquireRead() {
        return new LockResource(lock.readLock());
    }

    @Override
    public IResource acquireWrite() {
        return new LockResource(lock.writeLock());
    }
}
