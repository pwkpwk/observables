package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DispatchingObservableListTest {

	@Mock
	IListObserver<Integer> observer;
	
	@Captor
	ArgumentCaptor<IAction> actionCaptor;
	
	@Captor
	ArgumentCaptor<Collection<Integer>> collectionCaptor;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newDispatchingObservableListCopiesData() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		mol.mutator().add(Integer.valueOf(10));
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);

		assertEquals(1, dol.getSize());
		assertEquals(10, dol.getAt(0).intValue());
	}
	
	@Test
	public void addToSourceDispatchesUpdate() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		
		mol.mutator().add(Integer.valueOf(10));

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		assertEquals(1, actions.size());
		assertNotNull(actions.get(0));
		assertEquals(0, dol.getSize());
		actions.get(0).execute();
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void addToSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().add(Integer.valueOf(10));

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		actions.get(0).execute();
		
		verify(observer).added(eq(0), eq(1));
	}
	
	@Test
	public void removeFromSourceDispatchesRemoval() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		
		mol.mutator().remove(1, 5);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		assertEquals(1, actions.size());
		assertNotNull(actions.get(0));
		assertEquals(10, dol.getSize());
		actions.get(0).execute();
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void removeFromSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().remove(1, 5);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
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
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().move(1, 5, 3);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		actions.get(0).execute();
		
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
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().move(1, 3, 5);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		actions.get(0).execute();
		
		verify(observer, times(1)).moved(eq(1), eq(3), eq(5));
		assertListsEqual(mol.list(), dol);
	}
	
	@Test
	public void moveDownInSourceNotifies() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		ObservableList<Integer> mol = ObservableCollections.createObservableList();
		for (int i = 0; i < 10; ++i) {
			mol.mutator().add(Integer.valueOf(i));
		}
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().move(6, 0, 3);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		actions.get(0).execute();
		
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
		DispatchingObservableList<Integer> dol = new DispatchingObservableList<Integer>(mol.list(), dispatcher);
		dol.addObserver(observer);
		
		mol.mutator().move(2, 0, 3);

		verify(dispatcher, times(1)).dispatch(actionCaptor.capture());
		List<IAction> actions = actionCaptor.getAllValues();
		actions.get(0).execute();
		
		verify(observer, times(1)).moved(eq(2), eq(0), eq(3));
		assertListsEqual(mol.list(), dol);
	}
	
	private static <T> void assertListsEqual(IReadOnlyObservableList<T> list1, IReadOnlyObservableList<T> list2) {
		assertEquals(list1.getSize(), list2.getSize());
		for (int i = 0; i < list1.getSize(); ++i) {
			assertSame(list1.getAt(i), list2.getAt(i));
		}
	}
}
