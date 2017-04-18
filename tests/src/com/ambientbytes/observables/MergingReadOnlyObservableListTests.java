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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MergingReadOnlyObservableListTests {
	
	@Mock IListObserver observer;	
	@Mock ILinkedReadOnlyObservableList<Integer> integerList1;
	@Mock ILinkedReadOnlyObservableList<Integer> integerList2;
	@Mock ILinkedReadOnlyObservableList<Integer> integerList3;
	@Mock IReadWriteMonitor mockMonitor;
	@Mock IResource rLock;
	@Mock IResource wLock;	
	@Captor ArgumentCaptor<Collection<Integer>> integerCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockMonitor.acquireRead()).thenReturn(rLock);
		when(mockMonitor.acquireWrite()).thenReturn(wLock);
	}

	@Test
	public void newMergingReadOnlyObservableListCorrectSetup() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		
		assertEquals(0, mol.getSize());
	}

	@Test
	public void addOneListCopiesData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);

		sources.add(source);
		
		assertEquals(source.getSize(), mol.getSize());
		for (int i = 0; i < source.getSize(); ++i) {
			assertSame(source.getAt(i), mol.getAt(i));
		}
	}

	@Test
	public void addOneListNotifies() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mol.addObserver(observer);

		sources.add(source);
		
		verify(observer, times(1)).added(eq(0), eq(5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addSameListTwiceThrows() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);

		sources.add(source);
		sources.add(source);
	}

	@Test
	public void removeOnlyListClearsData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		sources.add(source);

		sources.remove(source);
		
		assertEquals(0, mol.getSize());
	}

	@Test
	public void removeOnlyListNotifies() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		final MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		sources.add(source);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				assertEquals(source.getSize(), mol.getSize());
				for (int i = 0; i < mol.getSize(); ++i) {
					assertSame(source.getAt(i), mol.getAt(i));
				}
				return null;
			}
		}).when(observer).removing(anyInt(), anyInt());
		mol.addObserver(observer);

		sources.remove(source);
		
		verify(observer, times(1)).removing(eq(0), eq(source.getSize()));
		verify(observer, times(1)).removed(eq(0), eq(source.getSize()));
	}

	@Test
	public void addListAddsObserver() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		new MergingReadOnlyObservableList<>(sources, monitor);
		when(integerList1.getSize()).thenReturn(0);
		sources.add(integerList1);
		
		verify(integerList1, times(1)).addObserver(any(IListObserver.class));
	}
	
	@Test
	public void removeListRemovesObserver() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		new MergingReadOnlyObservableList<>(sources, monitor);
		when(integerList1.getSize()).thenReturn(0);
		sources.add(integerList1);
		sources.remove(integerList1);
		
		verify(integerList1, times(1)).removeObserver(any(IListObserver.class));
	}

	@Test
	public void addThreeListsCopiesData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		mol.addObserver(observer);

		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		
		assertEquals(source1.getSize() + source2.getSize() + source3.getSize(), mol.getSize());
		int index = 0;
		for (int i = 0; i < source1.getSize(); ++i) {
			assertSame(source1.getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < source2.getSize(); ++i) {
			assertSame(source2.getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < source3.getSize(); ++i) {
			assertSame(source3.getAt(i), mol.getAt(index++));
		}
		verify(observer, times(1)).added(eq(0), eq(5));
		verify(observer, times(1)).added(eq(5), eq(5));
		verify(observer, times(1)).added(eq(10), eq(5));
	}

	@Test
	public void removeMiddleRemovesData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		sources.remove(source2);
		
		assertEquals(source1.getSize() + source3.getSize(), mol.getSize());
		int index = 0;
		for (int i = 0; i < source1.getSize(); ++i) {
			assertSame(source1.getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < source3.getSize(); ++i) {
			assertSame(source3.getAt(i), mol.getAt(index++));
		}
		verify(observer, times(1)).removing(eq(5), eq(5));
		verify(observer, times(1)).removed(eq(5), eq(5));
	}

	@Test
	public void moveMiddleMovesData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);

		mutator2.move(0, 2, 3);
		
		assertEquals(24, mol.getAt(5).intValue());
		assertEquals(25, mol.getAt(6).intValue());
		assertEquals(21, mol.getAt(7).intValue());
		assertEquals(22, mol.getAt(8).intValue());
		assertEquals(23, mol.getAt(9).intValue());

		int index = 0;
		for (int i = 0; i < source1.getSize(); ++i) {
			assertSame(source1.getAt(i), mol.getAt(index++));
		}
		index += 5;
		for (int i = 0; i < source3.getSize(); ++i) {
			assertSame(source3.getAt(i), mol.getAt(index++));
		}
	}

	@Test
	public void moveMiddleMoveReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		mutator2.move(0, 2, 3);

		verify(observer, times(1)).moved(eq(5), eq(7), eq(3));
	}

	@Test
	public void resetMiddleDownResetReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);

		mutator2.reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleDownResets() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);

		mutator2.reset(newData);

		assertEquals(source1.getSize() + source2.getSize() + source3.getSize(), mol.getSize());
		int i = 0;
		while (i < source1.getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize()) {
			assertEquals(51 + i - source1.getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize() + source3.getSize()) {
			assertEquals(31 + i - (source1.getSize() + source2.getSize()), mol.getAt(i).intValue());
			++i;
		}
	}

	@Test
	public void resetMiddleUpResetReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);
		newData.add(56);
		newData.add(57);

		mutator2.reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleUpResets() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);
		newData.add(56);
		newData.add(57);

		mutator2.reset(newData);

		assertEquals(source1.getSize() + source2.getSize() + source3.getSize(), mol.getSize());
		int i = 0;
		while (i < source1.getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize()) {
			assertEquals(51 + i - source1.getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize() + source3.getSize()) {
			assertEquals(31 + i - (source1.getSize() + source2.getSize()), mol.getAt(i).intValue());
			++i;
		}
	}

	@Test
	public void resetMiddleSameSizeResetReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);

		mutator2.reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleSameSizeResets() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);

		mutator2.reset(newData);

		assertEquals(source1.getSize() + source2.getSize() + source3.getSize(), mol.getSize());
		int i = 0;
		while (i < source1.getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize()) {
			assertEquals(51 + i - source1.getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < source1.getSize() + source2.getSize() + source3.getSize()) {
			assertEquals(31 + i - (source1.getSize() + source2.getSize()), mol.getAt(i).intValue());
			++i;
		}
	}
	
	@Test
	public void unlinkRemovesAllObservers() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		sources.add(integerList1);
		sources.add(integerList2);
		sources.add(integerList3);
		
		mol.unlink();
		
		verify(integerList1, times(1)).removeObserver(any(IListObserver.class));
		verify(integerList2, times(1)).removeObserver(any(IListObserver.class));
		verify(integerList3, times(1)).removeObserver(any(IListObserver.class));
	}
	
	@Test
	public void addToFirstAdded() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);

		mutator1.add(16);

		assertEquals(16, mol.getAt(5).intValue());
		assertEquals(21, mol.getAt(6).intValue());
		assertEquals(16, mol.getSize());
	}
	
	@Test
	public void addToFirstReports() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		mutator1.add(16);

		verify(observer, times(1)).added(5, 1);
	}
	
	@Test
	public void addToMiddleAdded() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);

		mutator2.add(26);

		assertEquals(26, mol.getAt(10).intValue());
		assertEquals(31, mol.getAt(11).intValue());
		assertEquals(16, mol.getSize());
	}
	
	@Test
	public void addCollectionToMiddleAdded() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		Collection<Integer> newValues = new ArrayList<>();
		newValues.add(26);
		newValues.add(27);
		newValues.add(28);

		mutator2.add(5, newValues);

		assertEquals(26, mol.getAt(10).intValue());
		assertEquals(27, mol.getAt(11).intValue());
		assertEquals(28, mol.getAt(12).intValue());
		assertEquals(31, mol.getAt(13).intValue());
		assertEquals(18, mol.getSize());
	}
	
	@Test
	public void addToMiddleReports() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		mutator2.add(16);

		verify(observer, times(1)).added(10, 1);
	}
	
	@Test
	public void addRemoveMiddleOriginalData() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);

		mutator2.add(26);
		mutator2.remove(5, 1);

		assertEquals(25, mol.getAt(9).intValue());
		assertEquals(31, mol.getAt(10).intValue());
		assertEquals(15, mol.getSize());
	}
	
	@Test
	public void removeMiddleReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		mutator2.remove(0, 1);

		assertEquals(22, mol.getAt(5).intValue());
		assertEquals(31, mol.getAt(9).intValue());
		assertEquals(14, mol.getSize());
		verify(observer, times(1)).removing(5, 1);
		verify(observer, times(1)).removed(5, 1);
	}
	
	@Test
	public void setOneMiddleReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		mol.addObserver(observer);

		mutator2.set(0, 51);

		assertEquals(51, mol.getAt(5).intValue());
		verify(observer, times(1)).changing(5, 1);
		verify(observer, times(1)).changed(5, 1);
	}
	
	@Test
	public void setCollectionMiddleReported() {
		IReadWriteMonitor monitor = new DummyReadWriteMonitor();
		MutableListSet<Integer> sources = new MutableListSet<Integer>(monitor); 
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(sources, monitor);
		final ListMutator<Integer> mutator1 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source1 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator1).build();
		mutator1.add(11);
		mutator1.add(12);
		mutator1.add(13);
		mutator1.add(14);
		mutator1.add(15);
		final ListMutator<Integer> mutator2 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source2 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator2).build();
		mutator2.add(21);
		mutator2.add(22);
		mutator2.add(23);
		mutator2.add(24);
		mutator2.add(25);
		final ListMutator<Integer> mutator3 = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source3 = ListBuilder.<Integer>create(mockMonitor).mutable(mutator3).build();
		mutator3.add(31);
		mutator3.add(32);
		mutator3.add(33);
		mutator3.add(34);
		mutator3.add(35);
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		Collection<Integer> newValues = new ArrayList<>();
		newValues.add(51);
		newValues.add(52);
		newValues.add(53);
		mol.addObserver(observer);

		mutator2.set(0, newValues);

		assertEquals(51, mol.getAt(5).intValue());
		assertEquals(52, mol.getAt(6).intValue());
		assertEquals(53, mol.getAt(7).intValue());
		verify(observer, times(1)).changing(5, 3);
		verify(observer, times(1)).changed(5, 3);
	}

}
