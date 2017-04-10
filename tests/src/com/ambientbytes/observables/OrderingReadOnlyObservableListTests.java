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
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

	@Captor ArgumentCaptor<Collection<Integer>> integerCollectionCaptor;
	@Captor ArgumentCaptor<Collection<TestItem>> testCollectionCaptor;
	@Mock IListObserver observer;
	private IReadWriteMonitor monitor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		monitor = new DummyReadWriteMonitor();
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
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), order, monitor);

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
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
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
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ool.addObserver(observer);
		ol.mutator().add(4);

		verify(observer, times(1)).added(eq(3), eq(1));
	}

	@Test
	public void removeLowestRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
		ol.mutator().remove(2, 1);
		
		assertEquals(2, ool.getAt(0).intValue());
		assertEquals(3, ool.getAt(1).intValue());
		assertEquals(4, ool.getAt(2).intValue());
		assertEquals(5, ool.getAt(3).intValue());
	}

	@Test
	public void removeLowestReportsRemoval() {
		final ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		final OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
		ool.addObserver(observer);
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				assertSame(ol.list().getAt(1), ool.getAt(2));
				return null;
			}
		}).when(observer).removing(eq(2), eq(1));
		ol.mutator().remove(1, 1);
		
		verify(observer, times(1)).removing(eq(2), eq(1));
		verify(observer, times(1)).removed(eq(2), eq(1));
	}

	@Test
	public void removeHighestRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
		ool.addObserver(observer);
		ol.mutator().remove(0, 1);
	
		assertEquals(4, ool.getSize());
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		verify(observer, times(1)).removing(eq(4), eq(1));
		verify(observer, times(1)).removed(eq(4), eq(1));
	}

	@Test
	public void removeMiddleRemoves() {
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(5);
		ol.mutator().add(3);
		ol.mutator().add(2);
		ol.mutator().add(1);
		ol.mutator().add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
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
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
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
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(ol.list(), new IntegerOrder(), monitor);
		ool.addObserver(observer);

		ool.setOrder(new IntegerReverseOrder());

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}
	
	@Test
	public void removeMutableUnadvises() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		
		assertNotNull(ool); // to suppress the warning about ool not being used
		assertEquals(1, item.getObserversNumber());
		ol.mutator().remove(2, 1);

		assertEquals(0, item.getObserversNumber());
	}
	
	@Test
	public void removeOneOfDuplicatesRemoves() {
		final ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		final TestItem item = new TestItem(2);
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item);
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(new TestItem(4));
		ol.mutator().add(new TestItem(5));
		final OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				boolean found = false;
				
				assertEquals(ol.list().getSize(), ool.getSize());
				for (int i = 0; i < ool.getSize(); ++i) {
					if (item == ool.getAt(i)) {
						found = true;
					}
				}
				assertTrue(found);
				return null;
			}
		}).when(observer).removing(anyInt(), anyInt());
		ool.addObserver(observer);

		ol.mutator().remove(4, 1);

		verify(observer, times(1)).removing(anyInt(), eq(1));
		verify(observer, times(1)).removed(anyInt(), eq(1));
	}
	
	@Test
	public void mutateItemDownListReordered() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		
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
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		
		item.setValue(9);

		assertEquals(1, ool.getAt(0).value);
		assertEquals(2, ool.getAt(1).value);
		assertEquals(4, ool.getAt(2).value);
		assertEquals(9, ool.getAt(3).value);
	}
	
	@Test
	public void mutateItemMoveReported() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		TestItem item;
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		ool.addObserver(observer);
		
		item.setValue(9);

		verify(observer, times(1)).moved(2, 3, 1);
	}
	
	@Test
	public void resetSourceItemsReplaced() {
		final TestItem[] originalItems = new TestItem[] { new TestItem(1), new TestItem(2), new TestItem(3), new TestItem(4), new TestItem(5) };
		final TestItem[] newItems = new TestItem[] { new TestItem(6), new TestItem(7), new TestItem(8), new TestItem(9) };
		final Collection<TestItem> newItemsList = new ArrayList<TestItem>(newItems.length);
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		for (TestItem item : originalItems) {
			ol.mutator().add(item);
		}
		Collections.addAll(newItemsList, newItems);
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);

		ol.mutator().reset(newItemsList);
		
		assertEquals(newItems.length, ool.getSize());
		for (int i = 0; i < newItems.length; ++i) {
			assertSame(newItems[i], ool.getAt(i));
			assertEquals(1, newItems[i].getObserversNumber());
		}
		for (TestItem item : originalItems) {
			assertEquals(0, item.getObserversNumber());
		}
	}
	
	@Test
	public void moveInSourceNoChange() {
		final TestItem[] originalItems = new TestItem[] { new TestItem(1), new TestItem(2), new TestItem(3), new TestItem(4), new TestItem(5) };
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		for (TestItem item : originalItems) {
			ol.mutator().add(item);
		}
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		ool.addObserver(observer);

		ol.mutator().move(0, 2, 2);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
		verify(observer, never()).added(anyInt(), anyInt());
		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
		verify(observer, never()).resetting();
		verify(observer, never()).reset();
	}
	
	@Test
	public void unlinkAndChangeNoChanges() {
		final TestItem[] originalItems = new TestItem[] { new TestItem(1), new TestItem(2), new TestItem(3), new TestItem(4), new TestItem(5) };
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		for (TestItem item : originalItems) {
			ol.mutator().add(item);
		}
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(ol.list(), new TestOrder(), monitor);
		ool.addObserver(observer);
		ool.unlink();

		ol.mutator().move(0, 2, 2);
		ol.mutator().add(new TestItem(100));
		ol.mutator().remove(0, 3);

		verify(observer, never()).moved(anyInt(), anyInt(), anyInt());
		verify(observer, never()).added(anyInt(), anyInt());
		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
		verify(observer, never()).resetting();
		verify(observer, never()).reset();
		for (int i = 0; i < originalItems.length; ++i) {
			assertSame(originalItems[i], ool.getAt(i));
			assertEquals(0, ool.getAt(i).getObserversNumber());
		}
	}

}
