package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderingReadOnlyObservableListTests {
	
	private static class IntegerOrder implements IItemsOrder<Integer> {
		@Override
		public boolean isLess(Integer lesser, Integer greater) {
			return lesser.intValue() < greater.intValue();
		}
	}
	
	private static class IntegerReverseOrder implements IItemsOrder<Integer> {
		@Override
		public boolean isLess(Integer lesser, Integer greater) {
			return greater.intValue() < lesser.intValue();
		}
	}

	@Captor
	ArgumentCaptor<Collection<Integer>> integerCollectionCaptor;
	
	@Mock
	IListObserver<Integer> integerObserver;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newOrderingReadOnlyObservableListSortsSourceItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		IItemsOrder order = new IntegerOrder(); 
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), order);

		assertSame(order, ool.getOrder());
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		assertEquals(5, ool.getAt(4).intValue());
	}

	@Test
	public void addToSourceSortsSourceItems() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		assertEquals(5, ool.getAt(4).intValue());
	}

	@Test
	public void removeLowestRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ol.mutator().remove(2, 1);
		
		assertEquals(2, ool.getAt(0).intValue());
		assertEquals(3, ool.getAt(1).intValue());
		assertEquals(4, ool.getAt(2).intValue());
		assertEquals(5, ool.getAt(3).intValue());
	}

	@Test
	public void removeHighestRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ool.addObserver(integerObserver);
		ol.mutator().remove(0, 1);
		
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		verify(integerObserver, times(1)).removed(eq(4), integerCollectionCaptor.capture());
		int[] expected = { 5 };
		int i = 0;
		assertEquals(expected.length, integerCollectionCaptor.getValue().size());
		for (Integer value : integerCollectionCaptor.getValue()) {
			assertEquals(expected[i++], value.intValue());
		}
	}

	@Test
	public void removeMiddleRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(2);
		ol.mutator().add(1);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ol.mutator().remove(1, 2);
		
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(4, ool.getAt(1).intValue());
		assertEquals(5, ool.getAt(2).intValue());
	}

	@Test
	public void changeOrderReorders() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(2);
		ol.mutator().add(1);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		IItemsOrder order = new IntegerReverseOrder(); 
		ool.setOrder(order);

		assertSame(order, ool.getOrder());
		assertEquals(5, ool.getAt(0).intValue());
		assertEquals(4, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(2, ool.getAt(3).intValue());
		assertEquals(1, ool.getAt(4).intValue());
	}

	@Test
	public void changeOrderReportsReset() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(2);
		ol.mutator().add(1);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ool.addObserver(integerObserver);

		ool.setOrder(new IntegerReverseOrder());

		verify(integerObserver, times(1)).reset(integerCollectionCaptor.capture());
		int[] expected = { 1, 2, 3, 4, 5 };
		int i = 0;
		assertEquals(expected.length, integerCollectionCaptor.getValue().size());
		for (Integer value : integerCollectionCaptor.getValue()) {
			assertEquals(expected[i++], value.intValue());
		}
	}

}
