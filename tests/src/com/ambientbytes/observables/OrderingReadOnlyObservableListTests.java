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
import static org.mockito.Mockito.when;

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
	@Mock IReadWriteMonitor mockMonitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	private IReadWriteMonitor monitor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockMonitor.acquireRead()).thenReturn(rLock);
		when(mockMonitor.acquireWrite()).thenReturn(wLock);
		monitor = new DummyReadWriteMonitor();
	}

	@Test
	public void newOrderingReadOnlyObservableListSortsSourceItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		mutator.add(4);
		IItemsOrder<Integer> order = new IntegerOrder(); 
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
				source,
				new ImmutableObservableReference<>(order),
				monitor);

		assertEquals(source.getSize(), ool.getSize());
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		assertEquals(5, ool.getAt(4).intValue());
	}

	@Test
	public void addToSourceSortsSourceItems() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
				source,
				new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
				monitor);
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		mutator.add(4);
		
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(2, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(4, ool.getAt(3).intValue());
		assertEquals(5, ool.getAt(4).intValue());
	}

	@Test
	public void addToSourceReportsAdding() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
                monitor);
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		ool.addObserver(observer);
		mutator.add(4);

		verify(observer, times(1)).added(eq(3), eq(1));
	}

	@Test
	public void removeLowestRemoves() {
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		mutator.add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
                monitor);
		mutator.remove(2, 1);
		
		assertEquals(2, ool.getAt(0).intValue());
		assertEquals(3, ool.getAt(1).intValue());
		assertEquals(4, ool.getAt(2).intValue());
		assertEquals(5, ool.getAt(3).intValue());
	}

	@Test
	public void removeLowestReportsRemoval() {
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		mutator.add(4);
		final OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
                monitor);
		ool.addObserver(observer);
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				assertSame(source.getAt(1), ool.getAt(2));
				return null;
			}
		}).when(observer).removing(eq(2), eq(1));
		mutator.remove(1, 1);
		
		verify(observer, times(1)).removing(eq(2), eq(1));
		verify(observer, times(1)).removed(eq(2), eq(1));
	}

	@Test
	public void removeHighestRemoves() {
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(1);
		mutator.add(2);
		mutator.add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
                monitor);
		ool.addObserver(observer);
		mutator.remove(0, 1);
	
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
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(2);
		mutator.add(1);
		mutator.add(4);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder()),
                monitor);
		mutator.remove(1, 2);
		
		assertEquals(1, ool.getAt(0).intValue());
		assertEquals(4, ool.getAt(1).intValue());
		assertEquals(5, ool.getAt(2).intValue());
	}

	@Test
	public void changeOrderReorders() {
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(2);
		mutator.add(1);
		mutator.add(4);
        MutableObservableReference<IItemsOrder<Integer>> order = new MutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder(), monitor);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(
		        source,
                order,
                monitor);
		order.setValue(new IntegerReverseOrder());

		assertEquals(5, ool.getAt(0).intValue());
		assertEquals(4, ool.getAt(1).intValue());
		assertEquals(3, ool.getAt(2).intValue());
		assertEquals(2, ool.getAt(3).intValue());
		assertEquals(1, ool.getAt(4).intValue());
	}

	@Test
	public void changeOrderReportsReset() {
		final ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(5);
		mutator.add(3);
		mutator.add(2);
		mutator.add(1);
		mutator.add(4);
        MutableObservableReference<IItemsOrder<Integer>> order = new MutableObservableReference<IItemsOrder<Integer>>(new IntegerOrder(), monitor);
		OrderingReadOnlyObservableList<Integer> ool = new OrderingReadOnlyObservableList<>(source, order, monitor);
		ool.addObserver(observer);

		order.setValue(new IntegerReverseOrder());

		verify(observer, times(1)).resetting();
		verify(observer, times(1)).reset();
	}
	
	@Test
	public void removeMutableUnadvises() {
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		TestItem item;
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		
		assertNotNull(ool); // to suppress the warning about ool not being used
		assertEquals(1, item.getObserversNumber());
		mutator.remove(2, 1);

		assertEquals(0, item.getObserversNumber());
	}
	
	@Test
	public void removeOneOfDuplicatesRemoves() {
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		final TestItem item = new TestItem(2);
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(new TestItem(2));
		mutator.add(new TestItem(2));
		mutator.add(item);
		mutator.add(new TestItem(2));
		mutator.add(new TestItem(4));
		mutator.add(new TestItem(5));
		final OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				boolean found = false;
				
				assertEquals(source.getSize(), ool.getSize());
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

		mutator.remove(4, 1);

		verify(observer, times(1)).removing(anyInt(), eq(1));
		verify(observer, times(1)).removed(anyInt(), eq(1));
	}
	
	@Test
	public void mutateItemDownListReordered() {
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		TestItem item;
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		
		item.setValue(0);

		assertEquals(0, ool.getAt(0).value);
		assertEquals(1, ool.getAt(1).value);
		assertEquals(2, ool.getAt(2).value);
		assertEquals(4, ool.getAt(3).value);
	}
	
	@Test
	public void mutateItemUpListReordered() {
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		TestItem item;
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		
		item.setValue(9);

		assertEquals(1, ool.getAt(0).value);
		assertEquals(2, ool.getAt(1).value);
		assertEquals(4, ool.getAt(2).value);
		assertEquals(9, ool.getAt(3).value);
	}
	
	@Test
	public void mutateItemMoveReported() {
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		TestItem item;
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(4));
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		ool.addObserver(observer);
		
		item.setValue(9);

		verify(observer, times(1)).moved(2, 3, 1);
	}
	
	@Test
	public void resetSourceItemsReplaced() {
		final TestItem[] originalItems = new TestItem[] { new TestItem(1), new TestItem(2), new TestItem(3), new TestItem(4), new TestItem(5) };
		final TestItem[] newItems = new TestItem[] { new TestItem(6), new TestItem(7), new TestItem(8), new TestItem(9) };
		final Collection<TestItem> newItemsList = new ArrayList<TestItem>(newItems.length);
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		for (TestItem item : originalItems) {
			mutator.add(item);
		}
		Collections.addAll(newItemsList, newItems);
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);

		mutator.reset(newItemsList);
		
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
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		for (TestItem item : originalItems) {
			mutator.add(item);
		}
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		ool.addObserver(observer);

		mutator.move(0, 2, 2);

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
		final ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		final IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
		for (TestItem item : originalItems) {
			mutator.add(item);
		}
		OrderingReadOnlyObservableList<TestItem> ool = new OrderingReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<IItemsOrder<TestItem>>(new TestOrder()),
                monitor);
		ool.addObserver(observer);
		ool.unlink();

		mutator.move(0, 2, 2);
		mutator.add(new TestItem(100));
		mutator.remove(0, 3);

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
