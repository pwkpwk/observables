package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class DispatchingObservableListTest {
	
	private static final class TestDispatcher implements IDispatcher {
		
		private final Collection<IAction> actions = new ArrayList<>();

		@Override
		public void dispatch(IAction action) {
			actions.add(action);
		}

		public int executeAll() {
			int count = actions.size();
			
			for (IAction action : actions) {
				action.execute();
			}
			actions.clear();
			
			return count;
		}
	}

	@Mock IListObserver observer;
	@Mock IReadWriteMonitor mockMonitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	@Captor ArgumentCaptor<IAction> actionCaptor;
	@Captor ArgumentCaptor<Collection<Integer>> collectionCaptor;
	private IReadWriteMonitor monitor;
	private TestDispatcher testDispatcher;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		monitor = new DummyReadWriteMonitor();
		when(mockMonitor.acquireRead()).thenReturn(rLock);
		when(mockMonitor.acquireWrite()).thenReturn(wLock);
		testDispatcher = new TestDispatcher();
	}

	@Test
	public void newDispatchingObservableListCopiesData() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(Integer.valueOf(10));
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, mockMonitor);

		assertEquals(1, testDispatcher.executeAll());
		assertEquals(1, dol.getSize());
		assertEquals(10, dol.getAt(0).intValue());
	}
	
	@Test
	public void addToSourceDispatchesUpdate() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, monitor);
		assertEquals(0, testDispatcher.executeAll());
		
		mutator.add(Integer.valueOf(10));

		assertEquals(0, dol.getSize());
		assertEquals(1, testDispatcher.executeAll());
		assertListsEqual(source, dol);
	}
	
	@Test
	public void addToSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.add(Integer.valueOf(10));

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer).added(eq(0), eq(1));
	}
	
	@Test
	public void removeFromSourceDispatchesRemoval() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, monitor);
		assertEquals(1, testDispatcher.executeAll());
		
		mutator.remove(1, 5);

		assertEquals(10, dol.getSize());
		assertEquals(1, testDispatcher.executeAll());
		assertListsEqual(source, dol);
	}
	
	@Test
	public void removeFromSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.remove(1, 5);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).removing(eq(1), eq(5));
		verify(observer, times(1)).removed(eq(1), eq(5));
	}
	
	@Test
	public void moveUpInSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.move(1, 5, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(1), eq(5), eq(3));
		assertListsEqual(source, dol);
	}
	
	@Test
	public void moveUpInSourceOverlapNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.move(1, 3, 5);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, atLeast(1)).moved(eq(1), eq(3), eq(5));
		assertListsEqual(source, dol);
	}
	
	@Test
	public void moveDownInSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.move(6, 0, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action :  actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(6), eq(0), eq(3));
		assertListsEqual(source, dol);
	}
	
	@Test
	public void moveDownInSourceOverlapNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.move(2, 0, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(2), eq(0), eq(3));
		assertListsEqual(source, dol);
	}
	
	@Test
	public void resetSourceDispatchesReset() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, dispatcher, monitor);
		dol.addObserver(observer);
		
		mutator.reset(newValues);

		verify(dispatcher, atLeast(2)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
		assertListsEqual(source, dol);
	}
	
	@Test
	public void resettingPreservesOriginalData() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		final DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, monitor);
		dol.addObserver(observer);
		assertEquals(1, testDispatcher.executeAll());
		final List<Integer> capturedValues = new ArrayList<>();
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				for (int i = 0; i < dol.getSize(); ++i) {
					capturedValues.add(dol.getAt(i));
				}
				return null;
			}
		}).when(observer).resetting();
		
		mutator.reset(newValues);
		assertEquals(2, testDispatcher.executeAll());

		assertEquals(10, capturedValues.size());
		for (int i = 0; i < 10; ++i) {
			assertEquals(i, capturedValues.get(i).intValue());
		}
	}
	
	@Test
	public void setCollectionDispatchesChange() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		final DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, monitor);
		assertEquals(1, testDispatcher.executeAll());
		
		mutator.set(1, newValues);
		assertEquals(1, dol.getAt(1).intValue());
		assertEquals(2, dol.getAt(2).intValue());
		assertEquals(1, testDispatcher.executeAll());
		assertEquals(20, dol.getAt(1).intValue());
		assertEquals(21, dol.getAt(2).intValue());
	}
	
	@Test
	public void setCollectionReportsChange() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		for (int i = 0; i < 10; ++i) {
			mutator.add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		final DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(source, testDispatcher, monitor);
		assertEquals(1, testDispatcher.executeAll());
		dol.addObserver(observer);
		doAnswer(new Answer<Void>(){
			@Override
			public Void answer(InvocationOnMock arg0) throws Throwable {
				assertEquals(1, dol.getAt(1).intValue());
				assertEquals(2, dol.getAt(2).intValue());
				return null;
			}
		}).when(observer).changing(eq(1), eq(2));
		
		mutator.set(1, newValues);
		assertEquals(1, testDispatcher.executeAll());
		
		verify(observer, times(1)).changing(eq(1), eq(2));
		verify(observer, times(1)).changed(eq(1), eq(2));
	}
	
	private static <T> void assertListsEqual(IReadOnlyObservableList<T> list1, IReadOnlyObservableList<T> list2) {
		assertEquals(list1.getSize(), list2.getSize());
		for (int i = 0; i < list1.getSize(); ++i) {
			assertSame(list1.getAt(i), list2.getAt(i));
		}
	}
}
