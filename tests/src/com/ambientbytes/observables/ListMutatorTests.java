package com.ambientbytes.observables;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ListMutatorTests {
	
	@Mock IReadWriteMonitor monitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	@Mock IListMutator<Integer> listener;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(monitor.acquireRead()).thenReturn(rLock);
		when(monitor.acquireWrite()).thenReturn(wLock);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addListenerTwiceThrows() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);

		mutator.addListener(listener);
		mutator.addListener(listener);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addNullListenerThrows() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);

		mutator.addListener(null);
	}
	
	@Test
	public void addSingleValueCallsAdd() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.add(1);
		
		verify(listener, times(1)).add(eq(1));
	}
	
	@Test
	public void addSingleValueAtIndexCallsAdd() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.add(1, 5);
		
		verify(listener, times(1)).add(eq(1), eq(5));
	}
	
	@Test
	public void addRangeAtIndexCallsAdd() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		Collection<Integer> range = new ArrayList<>();
		mutator.addListener(listener);
		
		mutator.add(1, range);
		
		verify(listener, times(1)).add(eq(1), eq(range));
	}
	
	@Test
	public void removeListenerRemoves() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.removeListener(listener);
		
		mutator.add(1);
		verify(listener, never()).add(any(Integer.class));
	}
	
	@Test
	public void setCallsSet() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.set(1, 5);
		
		verify(listener, times(1)).set(eq(1), eq(5));
	}
	
	@Test
	public void setRangeCallsSet() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		Collection<Integer> range = new ArrayList<>();
		mutator.addListener(listener);
		
		mutator.set(1, range);
		
		verify(listener, times(1)).set(eq(1), eq(range));
	}
	
	@Test
	public void removeCallsRemove() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.remove(1, 5);
		
		verify(listener, times(1)).remove(eq(1), eq(5));
	}
	
	@Test
	public void clearCallsClear() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.clear();
		
		verify(listener, times(1)).clear();
	}
	
	@Test
	public void moveCallsMove() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		mutator.addListener(listener);
		
		mutator.move(1, 2, 3);
		
		verify(listener, times(1)).move(eq(1), eq(2), eq(3));
	}
	
	@Test
	public void resetCallsReset() {
		ListMutator<Integer> mutator = new ListMutator<>(monitor);
		Collection<Integer> range = new ArrayList<>();
		mutator.addListener(listener);
		
		mutator.reset(range);
		
		verify(listener, times(1)).reset(eq(range));
	}

}
