package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashSet;

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
	
	private static final class TestItem implements IMutableObject {
		private final Collection<IObjectMutationObserver> observers;
		private int value;
		
		TestItem(int value) {
			this.observers = new HashSet<>();
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public void setValue(int value) {
			if (this.value != value) {
				this.value = value;
				for (IObjectMutationObserver observer : observers) {
					observer.mutated();
				}
			}
		}
		
		public int getObserversNumber() {
			return observers.size();
		}

		@Override
		public void addObserver(IObjectMutationObserver observer) {
			observers.add(observer);
		}

		@Override
		public void removeObserver(IObjectMutationObserver observer) {
			observers.remove(observer);
		}
	}
	
	private static final class TestOrder implements IItemsOrder<TestItem> {
		@Override
		public boolean isLess(TestItem lesser, TestItem greater) {
			return lesser.getValue() < greater.getValue();
		}
	}

	@Captor
	ArgumentCaptor<Collection<Integer>> integerCollectionCaptor;
	
	@Captor
	ArgumentCaptor<Collection<TestItem>> testCollectionCaptor;
	
	@Mock
	IListObserver<Integer> integerObserver;
	
	@Mock
	IListObserver<TestItem> testObserver;

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
		IItemsOrder<Integer> order = new IntegerOrder(); 
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), order);

		assertSame(order, ool.getOrder());
		assertEquals(ol.list().getSize(), ool.getSize());
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
	public void addToSourceReportsAdding() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ool.addObserver(integerObserver);
		ol.mutator().add(4);

		verify(integerObserver, times(1)).added(eq(3), eq(1));
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
	public void removeLowestReportsRemoval() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder());
		ool.addObserver(integerObserver);
		ol.mutator().remove(1, 1);
		
		verify(integerObserver, times(1)).removed(eq(2), integerCollectionCaptor.capture());
		assertEquals(Integer.valueOf(3), integerCollectionCaptor.getValue().toArray()[0]);
		assertEquals(1, integerCollectionCaptor.getValue().size());
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
		IItemsOrder<Integer> order = new IntegerReverseOrder(); 
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
	
	@Test
	public void removeMutableUnadvises() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder());
		
		assertEquals(1, item.getObserversNumber());
		ol.mutator().remove(2, 1);

		assertEquals(0, item.getObserversNumber());
	}
	
	@Test
	public void removeOneOfDuplicatesRemoves() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(2));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(4));
		ol.mutator().add(new TestItem(5));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder());
		ool.addObserver(testObserver);

		ol.mutator().remove(4, 1);

		verify(testObserver, times(1)).removed(anyInt(), testCollectionCaptor.capture());
		TestItem[] items = testCollectionCaptor.getValue().toArray(new TestItem[testCollectionCaptor.getValue().size()]);
		assertEquals(1, items.length);
		assertSame(item, items[0]);
	}
	
	@Test
	public void mutateItemDownListReordered() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder());
		
		item.setValue(0);

		assertEquals(0, ool.getAt(0).value);
		assertEquals(1, ool.getAt(1).value);
		assertEquals(2, ool.getAt(2).value);
		assertEquals(4, ool.getAt(3).value);
	}
	
	@Test
	public void mutateItemUpListReordered() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder());
		
		item.setValue(9);

		assertEquals(1, ool.getAt(0).value);
		assertEquals(2, ool.getAt(1).value);
		assertEquals(4, ool.getAt(2).value);
		assertEquals(9, ool.getAt(3).value);
	}

}
