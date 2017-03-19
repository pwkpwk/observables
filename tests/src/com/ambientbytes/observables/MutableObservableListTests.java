package com.ambientbytes.observables;

import static org.junit.Assert.*;

import java.util.Collection;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MutableObservableListTests {
	
	@Mock
	IListObserver<Integer> observer;
	
	@Captor
	ArgumentCaptor<Collection<Integer>> captor;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newAdd2ItemsAdded() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.addObserver(observer);
		mol.getMutator().insert(0, value1);
		mol.getMutator().insert(1, value2);

		assertEquals(2, mol.getSize());
		assertSame(value1, mol.getAt(0));
		assertSame(value2, mol.getAt(1));
		verify(observer, times(1)).added(0, 1);
		verify(observer, times(1)).added(1, 1);
	}
	
	@Test
	public void removeItemRemoved() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.getMutator().insert(0, value1);
		mol.getMutator().insert(1, value2);
		mol.addObserver(observer);
		mol.getMutator().remove(0, 1);

		assertEquals(1, mol.getSize());
		assertSame(value2, mol.getAt(0));
		verify(observer).removed(eq(0), captor.capture());
		assertEquals(1, captor.getValue().size());
		assertTrue(captor.getValue().contains(value1));
	}
	
	@Test
	public void clearCleared() {
		MutableObservableList<Object> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Object value1 = new Object();
		Object value2 = new Object();

		mol.getMutator().insert(0, value1);
		mol.getMutator().insert(1, value2);
		mol.getMutator().clear();

		assertEquals(0, mol.getSize());
	}
	
	@Test
	public void moveInTheMiddleUpMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 0, 1, 7, 8, 2, 3, 4, 5, 6, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(2, 4, 5);

		assertEquals(target.length, mol.getSize());
		assertEquals(target.length, origin.length);
		for (int i = 0; i < target.length; ++i) {
			assertEquals(target[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void moveBeginningNoOverlapUpMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 2, 3, 4, 5, 0, 1, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, 4, 2);

		assertEquals(target.length, mol.getSize());
		assertEquals(target.length, origin.length);
		for (int i = 0; i < target.length; ++i) {
			assertEquals(target[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void moveBeginningOverlapUpMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 5, 6, 0, 1, 2, 3, 4, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, 2, 5);

		assertEquals(target.length, mol.getSize());
		assertEquals(target.length, origin.length);
		for (int i = 0; i < target.length; ++i) {
			assertEquals(target[i], mol.getAt(i).intValue());
		}
	}

	@Test
	public void moveInTheMiddleDownMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 0, 1, 4, 5, 6, 7, 8, 2, 3, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		assertEquals(target.length, origin.length);
		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(4, 2, 5);

		assertEquals(target.length, mol.getSize());
		for (int i = 0; i < target.length; ++i) {
			assertEquals(target[i], mol.getAt(i).intValue());
		}
	}

}
