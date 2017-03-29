package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LockToolTests {
	
	@Mock ReadWriteLock rwLock;
	@Mock Lock lock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void acquireReadLockAcquires() {
		when(rwLock.readLock()).thenReturn(lock);
		
		LockTool.acquireReadLock(rwLock);
		
		verify(rwLock, times(1)).readLock();
		verify(rwLock, never()).writeLock();
		verify(lock, times(1)).lock();
		verify(lock, never()).unlock();
	}

	@Test
	public void acquireWriteLockAcquires() {
		when(rwLock.writeLock()).thenReturn(lock);
		
		LockTool.acquireWriteLock(rwLock);
		
		verify(rwLock, never()).readLock();
		verify(rwLock, times(1)).writeLock();
		verify(lock, times(1)).lock();
		verify(lock, never()).unlock();
	}

	@Test
	public void releaseReadLockResourceReleasesLock() {
		when(rwLock.readLock()).thenReturn(lock);
		IResource resource = LockTool.acquireReadLock(rwLock);

		resource.release();
		
		verify(lock, times(1)).unlock();
	}

	@Test
	public void releaseWriteLockResourceReleasesLock() {
		when(rwLock.writeLock()).thenReturn(lock);
		IResource resource = LockTool.acquireWriteLock(rwLock);

		resource.release();
		
		verify(lock, times(1)).unlock();
	}

}
