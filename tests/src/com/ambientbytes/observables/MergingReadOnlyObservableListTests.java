package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MergingReadOnlyObservableListTests {
	
	@Mock
	IListObserver<Integer> integerObserver;
	
	@Mock
	ILinkedReadOnlyObservableList<Integer> integerList;
	
	@Captor
	ArgumentCaptor<Collection<Integer>> integerCaptor;

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
		mol.addObserver(integerObserver);

		mol.add(ol.list());
		
		verify(integerObserver, times(1)).added(eq(0), eq(5));
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
		mol.addObserver(integerObserver);

		mol.remove(ol.list());
		
		verify(integerObserver, times(1)).removed(eq(0), integerCaptor.capture());
		int index = 0;
		for (Integer i : integerCaptor.getValue()) {
			assertSame(ol.list().getAt(index++), i);
		}
	}
	
	@Test
	public void addListAddsObserver() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		when(integerList.getSize()).thenReturn(0);
		mol.add(integerList);
		
		verify(integerList, times(1)).addObserver(any());
	}
	
	@Test
	public void removeListRemovesObserver() {
		MergingReadOnlyObservableList<Integer> mol = new MergingReadOnlyObservableList<>(new DummyReadWriteLock());
		when(integerList.getSize()).thenReturn(0);
		mol.add(integerList);
		mol.remove(integerList);
		
		verify(integerList, times(1)).removeObserver(any());
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
		mol.addObserver(integerObserver);

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
		verify(integerObserver, times(1)).added(eq(0), eq(5));
		verify(integerObserver, times(1)).added(eq(5), eq(5));
		verify(integerObserver, times(1)).added(eq(10), eq(5));
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
		mol.addObserver(integerObserver);

		mol.remove(ol2.list());
		
		assertEquals(ol1.list().getSize() + ol3.list().getSize(), mol.getSize());
		int index = 0;
		for (int i = 0; i < ol1.list().getSize(); ++i) {
			assertSame(ol1.list().getAt(i), mol.getAt(index++));
		}
		for (int i = 0; i < ol3.list().getSize(); ++i) {
			assertSame(ol3.list().getAt(i), mol.getAt(index++));
		}
		verify(integerObserver, times(1)).removed(eq(5), integerCaptor.capture());
		int itemIndex = 0;
		for (Integer i : integerCaptor.getValue()) {
			assertSame(ol2.list().getAt(itemIndex++), i);
		}
	}

}
