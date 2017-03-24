package com.ambientbytes.observables;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
	public void newListCorrectSetup() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		
		assertEquals(0, mol.getSize());
		assertNotNull(mol.getMutator());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void newListNullLockThrows() {
		new MutableObservableList<>(null);
	}

	@Test
	public void addLocksWrite() {
		ReadWriteLock rwLock = mock(ReadWriteLock.class);
		Lock rLock = mock(Lock.class);
		Lock wLock = mock(Lock.class);
		when(rwLock.readLock()).thenReturn(rLock);
		when(rwLock.writeLock()).thenReturn(wLock);
		MutableObservableList<Integer> mol = new MutableObservableList<>(rwLock);

		mol.getMutator().add(Integer.valueOf(1));

		verify(rwLock, atLeastOnce()).writeLock();
		verify(wLock, times(1)).lock();
		verify(wLock, times(1)).unlock();
	}

	@Test
	public void add2ItemsAdded() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.addObserver(observer);
		mol.getMutator().add(value1);
		mol.getMutator().add(value2);

		assertEquals(2, mol.getSize());
		assertSame(value1, mol.getAt(0));
		assertSame(value2, mol.getAt(1));
		verify(observer, times(1)).added(0, 1);
		verify(observer, times(1)).added(1, 1);
	}

	@Test
	public void insert2ItemsAdded() {
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
		assertEquals(1, mol.getMutator().remove(0, 1));

		assertEquals(1, mol.getSize());
		assertSame(value2, mol.getAt(0));
		verify(observer).removing(eq(0), eq(1));
		verify(observer).removed(eq(0), eq(1));
	}
	
	@Test
	public void removeTooManyTrimmedRemoved() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.getMutator().insert(0, value1);
		mol.getMutator().insert(1, value2);
		mol.addObserver(observer);
		assertEquals(1, mol.getMutator().remove(1, 10));
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
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveSourceNegativeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(-5, 2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationNegativeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(1, -2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveSourceOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(origin.length, 3, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, origin.length, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveOutsideThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, 2, origin.length);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void moveTooMuchThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(1, 0, origin.length);
	}
	
	@Test
	public void moveSamePositionNotMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().move(1, 1, 5);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void moveZeroLengthNotMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().move(1, 5, 0);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void removeZeroLengthNotRemoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().remove(1, 0);

		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
	}
	
	@Test
	public void emptyListClearNotRemoved() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		mol.addObserver(observer);
		
		mol.getMutator().clear();

		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void removeNegativeStartThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().remove(-1, 4);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void removeOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().remove(origin.length, 4);
	}
	
	@Test
	public void removeObserverAddNotReported() {
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		mol.addObserver(observer);
		mol.removeObserver(observer);
		
		mol.getMutator().add(Integer.valueOf(1));
		
		verify(observer, never()).added(anyInt(), anyInt());
	}
	
	@Test
	public void resetReset() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] updated = { 0, 1, 7, 8 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>(updated.length);

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < updated.length; ++i) {
			newContents.add(Integer.valueOf(updated[i]));
		}
		
		mol.getMutator().reset(newContents);

		assertEquals(updated.length, mol.getSize());
		for (int i = 0; i < updated.length; ++i) {
			assertEquals(updated[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void resetNotifies() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] updated = { 0, 1, 7, 8 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>(updated.length);

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().insert(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < updated.length; ++i) {
			newContents.add(Integer.valueOf(updated[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().reset(newContents);

		verify(observer, times(1)).reset(captor.capture());
		int i = 0;
		for (Integer v : captor.getValue()) {
			assertEquals(original[i++], v.intValue());
		}
	}

}
