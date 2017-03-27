package com.ambientbytes.observables;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Concurrency locking helper.
 * @author Pavel Karpenko
 *
 */
public final class LockTool {
	
	private static final class ReadLock implements IResource {
		
		private final Lock lock;
		
		ReadLock(Lock lock) {
			this.lock = lock;
			this.lock.lock();
		}

		@Override
		public void release() {
			lock.unlock();
		}
	}

	/**
	 * Acquire a read lock on a ReadWriteLock object and return a resource that must be released
	 * to release the lock.
	 * @param rwLock read-write lock
	 * @return IResource object that releases the acquired read lock upon release.
	 */
	public static IResource acquireReadLock(ReadWriteLock rwLock) {
		return new ReadLock(rwLock.readLock());
	}
	
	/**
	 * Acquire a write lock on a ReadWriteLock object and return a resource that must be released
	 * to release the lock.
	 * @param rwLock read-write lock
	 * @return IResource object that releases the acquired write lock upon release.
	 */
	public static IResource acquireWriteLock(ReadWriteLock rwLock) {
		return new ReadLock(rwLock.writeLock());
	}
}
