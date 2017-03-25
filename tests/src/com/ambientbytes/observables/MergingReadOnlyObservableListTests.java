package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MergingReadOnlyObservableListTests {
	
	@Mock IListObserver observer;	
	@Mock ILinkedReadOnlyObservableList<Integer> integerList1;
	@Mock ILinkedReadOnlyObservableList<Integer> integerList2;
	@Mock ILinkedReadOnlyObservableList<Integer> integerList3;
	
	@Captor ArgumentCaptor<Collection<Integer>> integerCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newMergingReadOnlyObservableListCorrectSetup() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		
		assertEquals(0, mol.getSize());
	}

	@Test
	public void addOneListCopiesData() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);

		mol.add(ol.list());
		
		assertEquals(ol.list().getSize(), mol.getSize());
		for (int i = 0; i < ol.list().getSize(); ++i) {
			assertSame(ol.list().getAt(i), mol.getAt(i));
		}
	}

	@Test
	public void addOneListNotifies() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		mol.addObserver(observer);

		mol.add(ol.list());
		
		verify(observer, times(1)).added(eq(0), eq(5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addSameListTwiceThrows() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);

		mol.add(ol.list());
		mol.add(ol.list());
	}

	@Test
	public void removeOnlyListClearsData() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		mol.add(ol.list());

		mol.remove(ol.list());
		
		assertEquals(0, mol.getSize());
	}

	@Test
	public void removeOnlyListNotifies() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		mol.add(ol.list());
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				assertEquals(ol.list().getSize(), mol.getSize());
				for (int i = 0; i < mol.getSize(); ++i) {
					assertSame(ol.list().getAt(i), mol.getAt(i));
				}
				return null;
			}
		}).when(observer).removing(anyInt(), anyInt());
		mol.addObserver(observer);

		mol.remove(ol.list());
		
		verify(observer, times(1)).removing(eq(0), eq(ol.list().getSize()));
		verify(observer, times(1)).removed(eq(0), eq(ol.list().getSize()));
	}

	@Test
	public void addListAddsObserver() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		when(integerList1.getSize()).thenReturn(0);
		mol.add(integerList1);
		
		verify(integerList1, times(1)).addObserver(any());
	}
	
	@Test
	public void removeListRemovesObserver() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		when(integerList1.getSize()).thenReturn(0);
		mol.add(integerList1);
		mol.remove(integerList1);
		
		verify(integerList1, times(1)).removeObserver(any());
	}

	@Test
	public void addThreeListsCopiesData() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.addObserver(observer);

		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		
		assertEquals(ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize(), mol.getSize());
		int index = 0;
		for (int i = 0; i < ol1.list().getSize(); ++i) {
			assertSame(ol1.list().getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < ol2.list().getSize(); ++i) {
			assertSame(ol2.list().getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < ol3.list().getSize(); ++i) {
			assertSame(ol3.list().getAt(i), mol.getAt(index++));
		}
		verify(observer, times(1)).added(eq(0), eq(5));
		verify(observer, times(1)).added(eq(5), eq(5));
		verify(observer, times(1)).added(eq(10), eq(5));
	}

	@Test
	public void removeMiddleRemovesData() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);

		mol.remove(ol2.list());
		
		assertEquals(ol1.list().getSize() + ol3.list().getSize(), mol.getSize());
		int index = 0;
		for (int i = 0; i < ol1.list().getSize(); ++i) {
			assertSame(ol1.list().getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < ol3.list().getSize(); ++i) {
			assertSame(ol3.list().getAt(i), mol.getAt(index++));
		}
		verify(observer, times(1)).removing(eq(5), eq(5));
		verify(observer, times(1)).removed(eq(5), eq(5));
	}

	@Test
	public void moveMiddleMovesData() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());

		ol2.mutator().move(0, 2, 3);
		
		assertEquals(24, mol.getAt(5).intValue());
		assertEquals(25, mol.getAt(6).intValue());
		assertEquals(21, mol.getAt(7).intValue());
		assertEquals(22, mol.getAt(8).intValue());
		assertEquals(23, mol.getAt(9).intValue());

		int index = 0;
		for (int i = 0; i < ol1.list().getSize(); ++i) {
			assertSame(ol1.list().getAt(i), mol.getAt(index++));
		}
		index += 5;
		for (int i = 0; i < ol3.list().getSize(); ++i) {
			assertSame(ol3.list().getAt(i), mol.getAt(index++));
		}
	}

	@Test
	public void moveMiddleMoveReported() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);

		ol2.mutator().move(0, 2, 3);

		verify(observer, times(1)).moved(eq(5), eq(7), eq(3));
	}

	@Test
	public void resetMiddleDownResetReported() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);

		ol2.mutator().reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleDownResets() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);

		ol2.mutator().reset(newData);

		assertEquals(ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize(), mol.getSize());
		int i = 0;
		while (i < ol1.list().getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize()) {
			assertEquals(51 + i - ol1.list().getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize()) {
			assertEquals(31 + i - (ol1.list().getSize() + ol2.list().getSize()), mol.getAt(i).intValue());
			++i;
		}
	}

	@Test
	public void resetMiddleUpResetReported() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);
		newData.add(56);
		newData.add(57);

		ol2.mutator().reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleUpResets() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);
		newData.add(56);
		newData.add(57);

		ol2.mutator().reset(newData);

		assertEquals(ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize(), mol.getSize());
		int i = 0;
		while (i < ol1.list().getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize()) {
			assertEquals(51 + i - ol1.list().getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize()) {
			assertEquals(31 + i - (ol1.list().getSize() + ol2.list().getSize()), mol.getAt(i).intValue());
			++i;
		}
	}

	@Test
	public void resetMiddleSameSizeResetReported() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);

		ol2.mutator().reset(newData);

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}

	@Test
	public void resetMiddleSameSizeResets() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		ObservableList<Integer> ol1 = ObservableCollections.createObservableList();
		ol1.mutator().add(11);
		ol1.mutator().add(12);
		ol1.mutator().add(13);
		ol1.mutator().add(14);
		ol1.mutator().add(15);
		ObservableList<Integer> ol2 = ObservableCollections.createObservableList();
		ol2.mutator().add(21);
		ol2.mutator().add(22);
		ol2.mutator().add(23);
		ol2.mutator().add(24);
		ol2.mutator().add(25);
		ObservableList<Integer> ol3 = ObservableCollections.createObservableList();
		ol3.mutator().add(31);
		ol3.mutator().add(32);
		ol3.mutator().add(33);
		ol3.mutator().add(34);
		ol3.mutator().add(35);
		mol.add(ol1.list());
		mol.add(ol2.list());
		mol.add(ol3.list());
		mol.addObserver(observer);
		List<Integer> newData = new ArrayList<>();
		newData.add(51);
		newData.add(52);
		newData.add(53);
		newData.add(54);
		newData.add(55);

		ol2.mutator().reset(newData);

		assertEquals(ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize(), mol.getSize());
		int i = 0;
		while (i < ol1.list().getSize()) {
			assertEquals(11 + i, mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize()) {
			assertEquals(51 + i - ol1.list().getSize(), mol.getAt(i).intValue());
			++i;
		}
		while (i < ol1.list().getSize() + ol2.list().getSize() + ol3.list().getSize()) {
			assertEquals(31 + i - (ol1.list().getSize() + ol2.list().getSize()), mol.getAt(i).intValue());
			++i;
		}
	}
	
	@Test
	public void unlinkRemovesAllObservers() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		mol.add(integerList1);
		mol.add(integerList2);
		mol.add(integerList3);
		
		mol.unlink();
		
		verify(integerList1, times(1)).removeObserver(any());
		verify(integerList2, times(1)).removeObserver(any());
		verify(integerList3, times(1)).removeObserver(any());
	}

}
