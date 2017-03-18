package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

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
		ListObserversCollection<Object> collection = new ListObserversCollection<>();
		
		collection.add(observer);
		collection.added(0, 1);
		
		verify(observer, times(1)).added(0, 1);
	}

	@Test
	public void reportRemovedReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>();
		Collection<Object> values = new ArrayList<Object>();
		
		collection.add(observer);
		collection.removed(0, values);
		
		verify(observer, times(1)).removed(0, values);
	}

	@Test
	public void reportMovedReported() {
		ListObserversCollection<Object> collection = new ListObserversCollection<>();
		
		collection.add(observer);
		collection.moved(0, 1, 5);
		
		verify(observer, times(1)).moved(0, 1, 5);
	}

}
