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
		IReadWriteMonitor monitor = LockTool.createReadWriteMonitor(rwLock);
		when(rwLock.readLock()).thenReturn(lock);

		monitor.acquireRead();

		verify(lock, times(1)).lock();
		verify(lock, never()).unlock();
        verify(rwLock, never()).writeLock();
	}

    @Test
    public void releaseReadLockReleases() {
        IReadWriteMonitor monitor = LockTool.createReadWriteMonitor(rwLock);
        when(rwLock.readLock()).thenReturn(lock);

        monitor.acquireRead().release();

        verify(lock, times(1)).lock();
        verify(lock, times(1)).unlock();
        verify(rwLock, never()).writeLock();
    }

    @Test
    public void acquireWriteLockAcquires() {
        IReadWriteMonitor monitor = LockTool.createReadWriteMonitor(rwLock);
        when(rwLock.writeLock()).thenReturn(lock);

        monitor.acquireWrite();

        verify(lock, times(1)).lock();
        verify(lock, never()).unlock();
        verify(rwLock, never()).readLock();
    }

    @Test
    public void releaseWriteLockReleases() {
        IReadWriteMonitor monitor = LockTool.createReadWriteMonitor(rwLock);
        when(rwLock.writeLock()).thenReturn(lock);

        monitor.acquireWrite().release();

        verify(lock, times(1)).lock();
        verify(lock, times(1)).unlock();
        verify(rwLock, never()).readLock();
    }

}
