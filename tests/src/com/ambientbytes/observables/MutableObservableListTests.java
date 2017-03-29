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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MutableObservableListTests {
	
	@Mock IListObserver observer;
	@Captor ArgumentCaptor<Collection<Integer>> captor;
	
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
		mol.getMutator().add(0, value1);
		mol.getMutator().add(1, value2);

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

		mol.getMutator().add(0, value1);
		mol.getMutator().add(1, value2);
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

		mol.getMutator().add(0, value1);
		mol.getMutator().add(1, value2);
		mol.addObserver(observer);
		assertEquals(1, mol.getMutator().remove(1, 10));
	}
	
	@Test
	public void clearCleared() {
		MutableObservableList<Object> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Object value1 = new Object();
		Object value2 = new Object();

		mol.getMutator().add(0, value1);
		mol.getMutator().add(1, value2);
		mol.getMutator().clear();

		assertEquals(0, mol.getSize());
	}
	
	@Test
	public void moveInTheMiddleUpMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 0, 1, 7, 8, 2, 3, 4, 5, 6, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(-5, 2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationNegativeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(1, -2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveSourceOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(origin.length, 3, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, origin.length, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveOutsideThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(0, 2, origin.length);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void moveTooMuchThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().move(1, 0, origin.length);
	}
	
	@Test
	public void moveSamePositionNotMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
		}
		
		mol.getMutator().remove(-1, 4);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void removeOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < origin.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(origin[i]));
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
			mol.getMutator().add(i, Integer.valueOf(original[i]));
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
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		final List<Integer> capturedValues = new ArrayList<>();
		Collection<Integer> newContents = new ArrayList<Integer>(updated.length);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				for (int i = 0; i < mol.getSize(); ++i) {
					capturedValues.add(mol.getAt(i));
				}
				return null;
			}
		}).when(observer).resetting();

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < updated.length; ++i) {
			newContents.add(Integer.valueOf(updated[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().reset(newContents);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
		assertEquals(original.length, capturedValues.size());
		for (int i = 0; i < original.length; ++i) {
			assertEquals(original[i], capturedValues.get(i).intValue());
		}
	}
	
	@Test
	public void addCollectionAllAdded() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] added = { 0, 1, 7, 8 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>(added.length);

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < added.length; ++i) {
			newContents.add(Integer.valueOf(added[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().add(0, newContents);

		verify(observer, times(1)).added(eq(0), eq(added.length));
		assertEquals(original.length + added.length, mol.getSize());
		for (int i = 0; i < added.length; ++i) {
			assertEquals(added[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void addEmptyCollectionNoChange() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>();

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().add(0, newContents);

		verify(observer, never()).added(anyInt(), anyInt());
		assertEquals(original.length, mol.getSize());
		for (int i = 0; i < original.length; ++i) {
			assertEquals(original[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void setOneSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(0, 100);
		
		assertEquals(100, mol.getAt(0).intValue());
	}
	
	@Test
	public void setLastOneSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());

		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(original.length - 1, 100);
		
		assertEquals(100, mol.getAt(original.length - 1).intValue());
	}
	
	@Test
	public void setCollectionSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(1, newContents);
		
		assertEquals(100, mol.getAt(1).intValue());
		assertEquals(101, mol.getAt(2).intValue());
		assertEquals(102, mol.getAt(3).intValue());
	}
	
	@Test
	public void setCollectionAtBackSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(original.length - newContents.size() - 1, newContents);
		
		assertEquals(100, mol.getAt(original.length - newContents.size() - 1).intValue());
		assertEquals(101, mol.getAt(original.length - newContents.size()).intValue());
		assertEquals(102, mol.getAt(original.length - newContents.size() + 1).intValue());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setOneNegativeIndexThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(-2, 456);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setOneBehindBackThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(original.length, 456);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setCollectionNegativeIndexThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(-1, newContents);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setCollectionBehindBackThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mol.getMutator().set(original.length - 1, newContents);
	}
	
	@Test
	public void setOneReports() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		doAnswer(new Answer<Void>(){
			@Override
			public Void answer(InvocationOnMock arg0) throws Throwable {
				assertEquals(0, mol.getAt(0).intValue());
				return null;
			}
		}).when(observer).changing(anyInt(), anyInt());
		
		mol.addObserver(observer);
		
		mol.getMutator().set(0, 100);
		
		verify(observer, times(1)).changing(eq(0), eq(1));
		verify(observer, times(1)).changed(eq(0), eq(1));
	}
	
	@Test
	public void setCollectionReports() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final MutableObservableList<Integer> mol = new MutableObservableList<>(new DummyReadWriteLock());
		for (int i = 0; i < original.length; ++i) {
			mol.getMutator().add(i, Integer.valueOf(original[i]));
		}
		final Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		doAnswer(new Answer<Void>(){
			@Override
			public Void answer(InvocationOnMock arg0) throws Throwable {
				assertEquals(0, mol.getAt(0).intValue());
				assertEquals(1, mol.getAt(1).intValue());
				assertEquals(2, mol.getAt(2).intValue());
				return null;
			}
		}).when(observer).changing(anyInt(), anyInt());
		
		mol.addObserver(observer);
		
		mol.getMutator().set(0, newContents);
		
		verify(observer, times(1)).changing(eq(0), eq(3));
		verify(observer, times(1)).changed(eq(0), eq(3));
	}

}
