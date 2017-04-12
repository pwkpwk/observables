package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MutableListSetTests {
	
	@Mock IReadWriteMonitor monitor;
	@Mock IReadOnlyObservableList<Integer> mockList;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newEmptyMutableListSetCorrectSetup() {
		when(monitor.acquireRead()).thenReturn(mock(IResource.class));
		when(monitor.acquireWrite()).thenReturn(mock(IResource.class));
		MutableListSet<Integer> set = new MutableListSet<>(monitor);
		
		assertFalse(set.iterator().hasNext());
	}

	@Test
	public void newNonEmptyMutableListSetCorrectSetup() {
		when(monitor.acquireRead()).thenReturn(mock(IResource.class));
		when(monitor.acquireWrite()).thenReturn(mock(IResource.class));
		MutableListSet<Integer> set = new MutableListSet<>(monitor, mockList);
		Iterator<IReadOnlyObservableList<Integer>> itr = set.iterator();
		
		assertTrue(itr.hasNext());
		assertSame(mockList, itr.next());
		assertFalse(itr.hasNext());
	}

}
