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
	@Mock IReadWriteMonitor mockMonitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	@Captor ArgumentCaptor<Collection<Integer>> captor;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockMonitor.acquireRead()).thenReturn(rLock);
		when(mockMonitor.acquireWrite()).thenReturn(wLock);
	}
	
	@Test
	public void newListCorrectSetup() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		
		assertEquals(0, mol.getSize());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void newListNullLockThrows() {
		new MutableObservableList<Integer>(new ListMutator<Integer>(mockMonitor), null);
	}

	@Test
	public void addLocksWrite() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		mutator.add(Integer.valueOf(1));

		verify(mockMonitor, times(2)).acquireWrite();
		verify(wLock, times(2)).release();
	}

	@Test
	public void add2ItemsAdded() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.addObserver(observer);
		mutator.add(value1);
		mutator.add(value2);

		assertEquals(2, mol.getSize());
		assertSame(value1, mol.getAt(0));
		assertSame(value2, mol.getAt(1));
		verify(observer, times(1)).added(0, 1);
		verify(observer, times(1)).added(1, 1);
	}

	@Test
	public void insert2ItemsAdded() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mol.addObserver(observer);
		mutator.add(0, value1);
		mutator.add(1, value2);

		assertEquals(2, mol.getSize());
		assertSame(value1, mol.getAt(0));
		assertSame(value2, mol.getAt(1));
		verify(observer, times(1)).added(0, 1);
		verify(observer, times(1)).added(1, 1);
	}
	
	@Test
	public void removeItemRemoved() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mutator.add(0, value1);
		mutator.add(1, value2);
		mol.addObserver(observer);
		mutator.remove(0, 1);

		assertEquals(1, mol.getSize());
		assertSame(value2, mol.getAt(0));
		verify(observer).removing(eq(0), eq(1));
		verify(observer).removed(eq(0), eq(1));
	}
	
	@Test
	public void removeTooManyTrimmedRemoved() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Integer value1 = Integer.valueOf(1);
		Integer value2 = Integer.valueOf(2);

		mutator.add(0, value1);
		mutator.add(1, value2);
		mol.addObserver(observer);
		mutator.remove(1, 10);
		assertEquals(1, mol.getSize());
		verify(observer, times(1)).removing(1, 1);
		verify(observer, times(1)).removed(1, 1);
	}
	
	@Test
	public void clearCleared() {
		ListMutator<Object> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Object> mol = new MutableObservableList<>(mutator, mockMonitor);
		Object value1 = new Object();
		Object value2 = new Object();

		mutator.add(0, value1);
		mutator.add(1, value2);
		mutator.clear();

		assertEquals(0, mol.getSize());
	}
	
	@Test
	public void moveInTheMiddleUpMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] target = { 0, 1, 7, 8, 2, 3, 4, 5, 6, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(2, 4, 5);

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
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(0, 4, 2);

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
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(0, 2, 5);

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
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		assertEquals(target.length, origin.length);
		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(4, 2, 5);

		assertEquals(target.length, mol.getSize());
		for (int i = 0; i < target.length; ++i) {
			assertEquals(target[i], mol.getAt(i).intValue());
		}
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveSourceNegativeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(-5, 2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationNegativeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(1, -2, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveSourceOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(origin.length, 3, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveDestinationOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(0, origin.length, 5);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void moveOutsideThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(0, 2, origin.length);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void moveTooMuchThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.move(1, 0, origin.length);
	}
	
	@Test
	public void moveSamePositionNotMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mutator.move(1, 1, 5);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void moveZeroLengthNotMoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mutator.move(1, 5, 0);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void removeZeroLengthNotRemoved() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		mol.addObserver(observer);
		
		mutator.remove(1, 0);

		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
	}
	
	@Test
	public void emptyListClearNotRemoved() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		mol.addObserver(observer);
		
		mutator.clear();

		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void removeNegativeStartThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.remove(-1, 4);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void removeOutsizeThrows() {
		final int[] origin = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < origin.length; ++i) {
			mutator.add(i, Integer.valueOf(origin[i]));
		}
		
		mutator.remove(origin.length, 4);
	}
	
	@Test
	public void removeObserverAddNotReported() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		mol.addObserver(observer);
		mol.removeObserver(observer);
		
		mutator.add(Integer.valueOf(1));
		
		verify(observer, never()).added(anyInt(), anyInt());
	}
	
	@Test
	public void resetReset() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] updated = { 0, 1, 7, 8 };
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>(updated.length);

		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < updated.length; ++i) {
			newContents.add(Integer.valueOf(updated[i]));
		}
		
		mutator.reset(newContents);

		assertEquals(updated.length, mol.getSize());
		for (int i = 0; i < updated.length; ++i) {
			assertEquals(updated[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void resetNotifies() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final int[] updated = { 0, 1, 7, 8 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
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
			mutator.add(i, Integer.valueOf(original[i]));
		}
		for (int i = 0; i < updated.length; ++i) {
			newContents.add(Integer.valueOf(updated[i]));
		}
		mol.addObserver(observer);
		
		mutator.reset(newContents);

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
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>(added.length);

		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, original[i]);
		}
		for (int i = 0; i < added.length; ++i) {
			newContents.add(added[i]);
		}
		mol.addObserver(observer);
		
		mutator.add(0, newContents);

		verify(observer, times(1)).added(eq(0), eq(added.length));
		assertEquals(original.length + added.length, mol.getSize());
		for (int i = 0; i < added.length; ++i) {
			assertEquals(added[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void addEmptyCollectionNoChange() {
		final int[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>();

		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.add(0, newContents);

		verify(observer, never()).added(anyInt(), anyInt());
		assertEquals(original.length, mol.getSize());
		for (int i = 0; i < original.length; ++i) {
			assertEquals(original[i], mol.getAt(i).intValue());
		}
	}
	
	@Test
	public void setOneSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(0, 100);
		
		assertEquals(100, mol.getAt(0).intValue());
	}
	
	@Test
	public void setLastOneSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);

		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(original.length - 1, 100);
		
		assertEquals(100, mol.getAt(original.length - 1).intValue());
	}
	
	@Test
	public void setCollectionSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(1, newContents);
		
		assertEquals(100, mol.getAt(1).intValue());
		assertEquals(101, mol.getAt(2).intValue());
		assertEquals(102, mol.getAt(3).intValue());
	}
	
	@Test
	public void setCollectionAtBackSets() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(original.length - newContents.size() - 1, newContents);
		
		assertEquals(100, mol.getAt(original.length - newContents.size() - 1).intValue());
		assertEquals(101, mol.getAt(original.length - newContents.size()).intValue());
		assertEquals(102, mol.getAt(original.length - newContents.size() + 1).intValue());
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setOneNegativeIndexThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(-2, 456);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setOneBehindBackThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(original.length, 456);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setCollectionNegativeIndexThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(-1, newContents);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void setCollectionBehindBackThrows() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		Collection<Integer> newContents = new ArrayList<Integer>();
		newContents.add(100);
		newContents.add(101);
		newContents.add(102);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		mol.addObserver(observer);
		
		mutator.set(original.length - 1, newContents);
	}
	
	@Test
	public void setOneReports() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
		}
		doAnswer(new Answer<Void>(){
			@Override
			public Void answer(InvocationOnMock arg0) throws Throwable {
				assertEquals(0, mol.getAt(0).intValue());
				return null;
			}
		}).when(observer).changing(anyInt(), anyInt());
		
		mol.addObserver(observer);
		
		mutator.set(0, 100);
		
		verify(observer, times(1)).changing(eq(0), eq(1));
		verify(observer, times(1)).changed(eq(0), eq(1));
	}
	
	@Test
	public void setCollectionReports() {
		final int[] original = { 0, 1, 2, 3, 4, 5 };
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final MutableObservableList<Integer> mol = new MutableObservableList<>(mutator, mockMonitor);
		for (int i = 0; i < original.length; ++i) {
			mutator.add(i, Integer.valueOf(original[i]));
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
		
		mutator.set(0, newContents);
		
		verify(observer, times(1)).changing(eq(0), eq(3));
		verify(observer, times(1)).changed(eq(0), eq(3));
	}

}
