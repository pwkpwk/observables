package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilteringReadOnlyObservableListTests {
	
	private static class TestItem implements IMutableObject {
		
		private final List<IObjectMutationObserver> observers;
		private int value;
		
		public TestItem(int value) {
			this.observers = new ArrayList<IObjectMutationObserver>();
			this.value = value;
		}

		@Override
		public void addObserver(IObjectMutationObserver observer) {
			observers.add(observer);
		}

		@Override
		public void removeObserver(IObjectMutationObserver observer) {
			observers.remove(observer);
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
	}

	private static class TestFilter implements IItemFilter<TestItem> {
		@Override
		public boolean isIn(TestItem item) {
			return item.getValue() < 10;
		}
	}
	
	@Mock IItemFilter<Integer> mockFilter1;
	@Mock IItemFilter<Integer> mockFilter2;
	@Mock IItemFilter<Object> mockObjectFilter;
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
	public void newListPermitAllAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
				source,
				new ImmutableObservableReference<>(mockFilter1),
				monitor);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void newListPermitNoneNoneAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
				source,
				new ImmutableObservableReference<>(mockFilter1),
				monitor);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}

	@Test
	public void changeFilterItemsDisappear() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		when(mockFilter2.isIn(any(Integer.class))).thenReturn(false);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
				source,
				filter,
				monitor);

		assertEquals(3, fol.getSize());
        filter.setValue(mockFilter2);
		verify(mockFilter2, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}

	@Test
	public void changeFilterItemsAppear() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		when(mockFilter2.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		assertEquals(0, fol.getSize());
		filter.setValue(mockFilter2);
		verify(mockFilter2, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void permitAllAddItemsAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void removePermittedRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.remove(2, 1);

		assertEquals(2, fol.getSize());
	}

	@Test
	public void removeDisallowedRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true).thenReturn(false).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.remove(1, 1);

		assertEquals(2, fol.getSize());
	}

	@Test
	public void removeRangeRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true).thenReturn(false).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.remove(0, 3);

		assertEquals(0, fol.getSize());
	}

	@Test
	public void permitNoneAddItemsNoneAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<Integer>> filter = new MutableObservableReference<>(mockFilter1, monitor);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);

		mutator.add(1);
		mutator.add(2);
		mutator.add(3);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}
	
	@Test
	public void mutateToAllowedItemAppears() {
		ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<TestItem>> filter = new MutableObservableReference<IItemFilter<TestItem>>(new TestFilter(), monitor);
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);
		TestItem item;
		
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(11));
		mutator.add(new TestItem(12));
		
		assertEquals(2, fol.getSize());
		item.setValue(3);
		assertEquals(3, fol.getSize());
	}
	
	@Test
	public void mutateToDisallowedItemDisappears() {
		ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<TestItem>> filter = new MutableObservableReference<IItemFilter<TestItem>>(new TestFilter(), monitor);
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);
		TestItem item;
		
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(12));
		
		assertEquals(3, fol.getSize());
		item.setValue(11);
		assertEquals(2, fol.getSize());
	}
	
	@Test
	public void mutateRemovedMutationIgnored() {
		ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
        MutableObservableReference<IItemFilter<TestItem>> filter = new MutableObservableReference<IItemFilter<TestItem>>(new TestFilter(), monitor);
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);
		TestItem item;
		
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		mutator.add(item = new TestItem(11));
		mutator.add(new TestItem(12));
		mutator.remove(2, 1);
		
		assertEquals(2, fol.getSize());
		item.setValue(3);
		assertEquals(2, fol.getSize());
	}
	
	@Test
	public void unlinkNoMoreUpdates() {
		ListMutator<TestItem> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<TestItem> source = ListBuilder.<TestItem>create(mockMonitor).mutable(mutator).build();
        ImmutableObservableReference<IItemFilter<TestItem>> filter = new ImmutableObservableReference<IItemFilter<TestItem>>(new TestFilter());
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(source, filter, monitor);
		TestItem item;
		
		mutator.add(new TestItem(1));
		mutator.add(new TestItem(2));
		fol.unlink();
		mutator.add(item = new TestItem(3));
		mutator.add(new TestItem(12));
		
		assertEquals(2, fol.getSize());
		item.setValue(11);
		assertEquals(2, fol.getSize());
		
		List<Object> l = new ArrayList<>();
		l.add(source.getAt(0));
		l.add(source.getAt(1));
		assertContainsAllItems(fol, l);
	}

	@Test
	public void changeOneVisibleItemToVisibleChangesInPlace() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);

		mutator.set(1, 5);
		
		assertEquals(5, fol.getAt(1).intValue());
	}

	@Test
	public void changeOneVisibleItemToVisibleReportsChange() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);

		mutator.set(1, 5);
		
		verify(observer, times(1)).changing(eq(1), eq(1));
		verify(observer, times(1)).changed(eq(1), eq(1));
	}

	@Test
	public void changeOneVisibleItemToInvisibleRemoves() {
		when(mockFilter1.isIn(any(Integer.class))).thenAnswer(new Answer<Boolean>(){
			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				return ((Integer)arg0.getArgument(0)).intValue() < 50 ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(50);
		mutator.add(51);
		mutator.add(52);
		mutator.add(53);
		mutator.add(54);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);

		mutator.set(1, 100);
		
		assertEquals(4, fol.getSize());
		verify(observer, times(1)).removing(eq(1), eq(1));
		verify(observer, times(1)).removed(eq(1), eq(1));
	}

	@Test
	public void changeOneInvisibleItemToVisibleAdds() {
		when(mockFilter1.isIn(any(Integer.class))).thenAnswer(new Answer<Boolean>(){
			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				return ((Integer)arg0.getArgument(0)).intValue() < 50 ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(2);
		mutator.add(3);
		mutator.add(4);
		mutator.add(5);
		mutator.add(50);
		mutator.add(51);
		mutator.add(52);
		mutator.add(53);
		mutator.add(54);
        FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
                source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);

		mutator.set(6, 6);
		
		assertEquals(6, fol.getSize());
		verify(observer, never()).removing(anyInt(), anyInt());
		verify(observer, never()).removed(anyInt(), anyInt());
		verify(observer, times(1)).added(anyInt(), eq(1));
	}

	@Test
	public void changeToMoreVisibleAdds() {
		when(mockFilter1.isIn(any(Integer.class))).thenAnswer(new Answer<Boolean>(){
			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				return ((Integer)arg0.getArgument(0)).intValue() < 50 ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(50);
		mutator.add(2);
		mutator.add(51);
		mutator.add(3);
		mutator.add(52);
		mutator.add(4);
		mutator.add(53);
		mutator.add(5);
		mutator.add(54);
		mutator.add(56);
		mutator.add(57);
		mutator.add(58);
		mutator.add(59);
		mutator.add(60);
        FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
                source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);
		Collection<Integer> newValues = new ArrayList<>();
		newValues.add(6);  // 2
		newValues.add(60); // 51
		newValues.add(61); // 3
		newValues.add(62); // 52
		newValues.add(63); // 4
		newValues.add(7);  // 53
		newValues.add(8);  // 5
		newValues.add(9);  // 54
		newValues.add(10); // 56
		newValues.add(11); // 57
		newValues.add(12); // 58

		mutator.set(2, newValues);
		
		assertEquals(8, fol.getSize());
		List<Object> values = new ArrayList<>();
		values.add(Integer.valueOf(1));
		values.add(Integer.valueOf(6));
		values.add(Integer.valueOf(7));
		values.add(Integer.valueOf(8));
		values.add(Integer.valueOf(9));
		values.add(Integer.valueOf(10));
		values.add(Integer.valueOf(11));
		values.add(Integer.valueOf(12));
		assertContainsAllItems(fol, values);
	}

	@Test
	public void changeToFewerVisibleAdds() {
		when(mockFilter1.isIn(any(Integer.class))).thenAnswer(new Answer<Boolean>(){
			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				return ((Integer)arg0.getArgument(0)).intValue() < 50 ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(50);
		mutator.add(2);
		mutator.add(51);
		mutator.add(3);
		mutator.add(52);
		mutator.add(4);
		mutator.add(53);
		mutator.add(5);
		mutator.add(54);
		mutator.add(56);
		mutator.add(57);
		mutator.add(58);
		mutator.add(59);
		mutator.add(60);
        FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
                source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);
		Collection<Integer> newValues = new ArrayList<>();
		newValues.add(100); // 2
		newValues.add(60);  // 51
		newValues.add(61);  // 3
		newValues.add(62);  // 52
		newValues.add(63);  // 4
		newValues.add(7);   // 53
		newValues.add(101); // 5
		newValues.add(102); // 54
		newValues.add(103); // 56
		newValues.add(104); // 57
		newValues.add(12);  // 58

		mutator.set(2, newValues);
		
		assertEquals(3, fol.getSize());
		List<Object> values = new ArrayList<>();
		values.add(Integer.valueOf(1));
		values.add(Integer.valueOf(7));
		values.add(Integer.valueOf(12));
		assertContainsAllItems(fol, values);
	}
	
	@Test
	public void resetResetsContents() {
		when(mockFilter1.isIn(any(Integer.class))).thenAnswer(new Answer<Boolean>(){
			@Override
			public Boolean answer(InvocationOnMock arg0) throws Throwable {
				return ((Integer)arg0.getArgument(0)).intValue() < 50 ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		ListMutator<Integer> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Integer> source = ListBuilder.<Integer>create(mockMonitor).mutable(mutator).build();
		mutator.add(1);
		mutator.add(50);
		mutator.add(2);
		mutator.add(51);
		mutator.add(3);
		mutator.add(52);
		mutator.add(4);
		mutator.add(53);
		mutator.add(5);
		mutator.add(54);
		mutator.add(56);
		mutator.add(57);
		mutator.add(58);
		mutator.add(59);
		mutator.add(60);
        FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(
                source,
                new ImmutableObservableReference<>(mockFilter1),
                monitor);
		fol.addObserver(observer);
		Collection<Integer> newValues = new ArrayList<>();
		newValues.add(30);
		newValues.add(31);
		newValues.add(32);
		newValues.add(33);
		newValues.add(201);
		newValues.add(202);
		Collection<Object> testValues = new ArrayList<>();
		testValues.add(30);
		testValues.add(31);
		testValues.add(32);
		testValues.add(33);

		mutator.reset(newValues);
		
		assertEquals(4, fol.getSize());
		assertContainsAllItems(fol, testValues);
	}
	
	@Test
	public void resetResetsUnadvisesObjects() {
		when(mockObjectFilter.isIn(any())).thenReturn(true);
		IMutableObject mutable1 = mock(IMutableObject.class);
		IMutableObject mutable2 = mock(IMutableObject.class);
		ListMutator<Object> mutator = new ListMutator<>(mockMonitor);
		IReadOnlyObservableList<Object> source = ListBuilder.create(mockMonitor).mutable(mutator).build();
		mutator.add(mutable1);
		FilteringReadOnlyObservableList<Object> fol = new FilteringReadOnlyObservableList<>(
		        source,
                new ImmutableObservableReference<>(mockObjectFilter),
                mockMonitor);
		fol.addObserver(observer);
		Collection<Object> newValues = new ArrayList<>();
		newValues.add(mutable2);

		verify(mutable1, times(1)).addObserver(any(IObjectMutationObserver.class));
		verify(mutable1, never()).removeObserver(any(IObjectMutationObserver.class));
		mutator.reset(newValues);
		
		assertEquals(1, fol.getSize());
		assertSame(mutable2, fol.getAt(0));
		verify(mutable1, times(1)).removeObserver(any(IObjectMutationObserver.class));
		verify(mutable2, times(1)).addObserver(any(IObjectMutationObserver.class));
		verify(mutable2, never()).removeObserver(any(IObjectMutationObserver.class));
	}

	private static <T> void assertContainsAllItems(IReadOnlyObservableList<T> list, Collection<Object> items) {
		List<Object> copy = new ArrayList<>(items);
		
		for (int i = 0; i < list.getSize(); ++i) {
			assertTrue(copy.contains(list.getAt(i)));
			copy.remove(list.getAt(i));
		}
		assertEquals(items.size(), list.getSize());
		assertTrue(copy.isEmpty());
	}
}
