package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ListObserversCollectionTests {
	
	@Mock
	IListObserver<Object> observer;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void reportAddedReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.added(0, 1);
		
		verify(observer, times(1)).added(0, 1);
	}

	@Test
	public void removeObserverNoEvents() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.remove(observer);
		collection.added(0, 1);
		
		verify(observer, never()).added(Mockito.anyInt(), Mockito.anyInt());
	}

	@Test
	public void reportRemovedReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>(new DummyReadWriteLock());
		Collection<Object> values = new ArrayList<Object>();
		
		collection.add(observer);
		collection.removed(0, values);
		
		verify(observer, times(1)).removed(0, values);
	}

	@Test
	public void reportMovedReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>(new DummyReadWriteLock());
		
		collection.add(observer);
		collection.moved(0, 1, 5);
		
		verify(observer, times(1)).moved(0, 1, 5);
	}

	@Test
	public void reportResetReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>(new DummyReadWriteLock());
		Collection<Object> values = new ArrayList<Object>();
		
		collection.add(observer);
		collection.reset(values);
		
		verify(observer, times(1)).reset(values);
	}

	@Test
	public void addObserverWriteLockUsed() {
		Lock lock = mock(Lock.class);
		ReadWriteLock monitor = mock(ReadWriteLock.class);
		when(monitor.readLock()).thenReturn(lock);
		when(monitor.writeLock()).thenReturn(lock);
		ListObserversCollection<Object> collection = new ListObserversCollection<>(monitor);
		
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
		ListObserversCollection<Object> collection = new ListObserversCollection<>(monitor);
		
		collection.remove(observer);

		verify(monitor, never()).readLock();
		verify(monitor, times(1)).writeLock();
		verify(lock, times(1)).lock();
		verify(lock, times(1)).unlock();
	}

}
