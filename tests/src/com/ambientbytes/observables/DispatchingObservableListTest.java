package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	@Captor ArgumentCaptor<IAction> actionCaptor;
	@Captor ArgumentCaptor<Collection<Integer>> collectionCaptor;
	private ReadWriteLock lock;
	private TestDispatcher testDispatcher;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		lock = new DummyReadWriteLock();
		testDispatcher = new TestDispatcher();
	}

	@Test
	public void newDispatchingObservableListCopiesData() {
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		mol.mutator().add(Integer.valueOf(10));
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), testDispatcher, lock);

		assertEquals(1, testDispatcher.executeAll());
		assertEquals(1, dol.getSize());
		assertEquals(10, dol.getAt(0).intValue());
	}
	
	@Test
	public void addToSourceDispatchesUpdate() {
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), testDispatcher, lock);
		assertEquals(0, testDispatcher.executeAll());
		
		mol.mutator().add(Integer.valueOf(10));

		assertEquals(0, dol.getSize());
		assertEquals(1, testDispatcher.executeAll());
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void addToSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().add(Integer.valueOf(10));

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer).added(eq(0), eq(1));
	}
	
	@Test
	public void removeFromSourceDispatchesRemoval() {
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), testDispatcher, lock);
		assertEquals(1, testDispatcher.executeAll());
		
		mol.mutator().remove(1, 5);

		assertEquals(10, dol.getSize());
		assertEquals(1, testDispatcher.executeAll());
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void removeFromSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().remove(1, 5);

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
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().move(1, 5, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(1), eq(5), eq(3));
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void moveUpInSourceOverlapNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().move(1, 3, 5);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, atLeast(1)).moved(eq(1), eq(3), eq(5));
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void moveDownInSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().move(6, 0, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action :  actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(6), eq(0), eq(3));
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void moveDownInSourceOverlapNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().move(2, 0, 3);

		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).moved(eq(2), eq(0), eq(3));
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void resetSourceDispatchesReset() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher, lock);
		dol.addObserver(observer);
		
		mol.mutator().reset(newValues);

		verify(dispatcher, atLeast(2)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void resettingPreservesOriginalData() {
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		List<Integer> newValues = new ArrayList<>();
		newValues.add(20);
		newValues.add(21);
		final DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), testDispatcher, lock);
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
		
		mol.mutator().reset(newValues);
		assertEquals(2, testDispatcher.executeAll());

		assertEquals(10, capturedValues.size());
		for (int i = 0; i < 10; ++i) {
			assertEquals(i, capturedValues.get(i).intValue());
		}
	}
	
	private static <T> void assertListsEqual(IReadOnlyObservableList<T> list1, IReadOnlyObservableList<T> list2) {
		assertEquals(list1.getSize(), list2.getSize());
		for (int i = 0; i < list1.getSize(); ++i) {
			assertSame(list1.getAt(i), list2.getAt(i));
		}
	}
}
