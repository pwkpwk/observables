package com.ambientbytes.observables;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

final class DummyReadWriteLock implements ReadWriteLock {
	
	private final Lock lock;
	
	private static class DummyLock implements Lock
	{
		@Override
		public void lock() {}
		@Override
		public void lockInterruptibly() throws InterruptedException {}
		@Override
		public boolean tryLock() { return false; }
		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException { return false; }
		@Override
		public void unlock() {}
		@Override
		public Condition newCondition() { return null; }
	}
	
	public DummyReadWriteLock() {
		this.lock = new DummyLock();
	}

	@Override
	public Lock readLock() {
		return lock;
	}

	@Override
	public Lock writeLock() {
		return lock;
	}

}
