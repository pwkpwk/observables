package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListObserversTests {
	
	@Mock IListObserver observer;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void reportAddedReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.added(0, 1);
		
		verify(observer, times(1)).added(0, 1);
	}

	@Test
	public void removeObserverNoEvents() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.remove(observer);
		collection.added(0, 1);
		
		verify(observer, never()).added(Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void reportRemovingReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.removing(0, 1);
		
		verify(observer, times(1)).removing(0, 1);
	}

	@Test
	public void reportRemovedReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.removed(0, 1);
		
		verify(observer, times(1)).removed(0, 1);
	}

	@Test
	public void reportMovedReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.moved(0, 1, 5);
		
		verify(observer, times(1)).moved(0, 1, 5);
	}

	@Test
	public void reportResettingReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.resetting();
		
		verify(observer, times(1)).resetting();
	}

	@Test
	public void reportResetReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.reset();
		
		verify(observer, times(1)).reset();
	}

	@Test
	public void addObserverWriteLockUsed() {
		Lock lock = mock(Lock.class);
		ReadWriteLock monitor = mock(ReadWriteLock.class);
		when(monitor.readLock()).thenReturn(lock);
		when(monitor.writeLock()).thenReturn(lock);
		ListObservers<Object> collection = new ListObservers<>(monitor);
		
		collection.add(observer);

		verify(monitor, never()).readLock();
		verify(monitor, times(1)).writeLock();
		verify(lock, times(1)).lock();
		verify(lock, times(1)).unlock();
	}

	@Test
	public void removeObserverWriteLockUsed() {
		Lock lock = mock(Lock.class);
		ReadWriteLock monitor = mock(ReadWriteLock.class);
		when(monitor.readLock()).thenReturn(lock);
		when(monitor.writeLock()).thenReturn(lock);
		ListObservers<Object> collection = new ListObservers<>(monitor);
		
		collection.remove(observer);

		verify(monitor, never()).readLock();
		verify(monitor, times(1)).writeLock();
		verify(lock, times(1)).lock();
		verify(lock, times(1)).unlock();
	}
	
	@Test
	public void reportChangingReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.changing(0, 10);
		
		verify(observer, times(1)).changing(eq(0), eq(10));
	}
	
	@Test
	public void reportChangedReported() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.changed(0, 10);
		
		verify(observer, times(1)).changed(eq(0), eq(10));
	}
	
	@Test(expected = IllegalStateException.class)
	public void addObserverTwiceThrows() {
		ListObservers<Object> collection = new ListObservers<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.add(observer);
	}

}
