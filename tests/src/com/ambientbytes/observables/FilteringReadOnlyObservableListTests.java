package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	@Mock IListObserver observer;
	private ReadWriteLock lock;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		lock = new DummyReadWriteLock();
	}

	@Test
	public void newListPermitAllAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void newListPermitNoneNoneAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}

	@Test
	public void changeFilterItemsDisappear() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		when(mockFilter2.isIn(any(Integer.class))).thenReturn(false);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		assertEquals(3, fol.getSize());
		fol.setFilter(mockFilter2);
		verify(mockFilter2, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}

	@Test
	public void changeFilterItemsAppear() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		when(mockFilter2.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		assertEquals(0, fol.getSize());
		fol.setFilter(mockFilter2);
		verify(mockFilter2, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void permitAllAddItemsAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void removePermittedRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().remove(2, 1);

		assertEquals(2, fol.getSize());
	}

	@Test
	public void removeDisallowedRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true).thenReturn(false).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().remove(1, 1);

		assertEquals(2, fol.getSize());
	}

	@Test
	public void removeRangeRemoved() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true).thenReturn(false).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().remove(0, 3);

		assertEquals(0, fol.getSize());
	}

	@Test
	public void permitNoneAddItemsNoneAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}
	
	@Test
	public void mutateToAllowedItemAppears() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter(), lock);
		TestItem item;
		
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(11));
		ol.mutator().add(new TestItem(12));
		
		assertEquals(2, fol.getSize());
		item.setValue(3);
		assertEquals(3, fol.getSize());
	}
	
	@Test
	public void mutateToDisallowedItemDisappears() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter(), lock);
		TestItem item;
		
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(12));
		
		assertEquals(3, fol.getSize());
		item.setValue(11);
		assertEquals(2, fol.getSize());
	}
	
	@Test
	public void mutateRemovedMutationIgnored() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter(), lock);
		TestItem item;
		
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		ol.mutator().add(item = new TestItem(11));
		ol.mutator().add(new TestItem(12));
		ol.mutator().remove(2, 1);
		
		assertEquals(2, fol.getSize());
		item.setValue(3);
		assertEquals(2, fol.getSize());
	}
	
	@Test
	public void unlinkNoMoreUpdates() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter(), lock);
		TestItem item;
		
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		fol.unlink();
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(12));
		
		assertEquals(2, fol.getSize());
		item.setValue(11);
		assertEquals(2, fol.getSize());
		
		List<Object> l = new ArrayList<>();
		l.add(ol.list().getAt(0));
		l.add(ol.list().getAt(1));
		assertContainsAllItems(fol, l);
	}

	@Test
	public void changeOneVisibleItemToVisibleChangesInPlace() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);

		ol.mutator().set(1, 5);
		
		assertEquals(5, fol.getAt(1).intValue());
	}

	@Test
	public void changeOneVisibleItemToVisibleReportsChange() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);
		fol.addObserver(observer);

		ol.mutator().set(1, 5);
		
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
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(50);
		ol.mutator().add(51);
		ol.mutator().add(52);
		ol.mutator().add(53);
		ol.mutator().add(54);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);
		fol.addObserver(observer);

		ol.mutator().set(1, 100);
		
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
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().add(4);
		ol.mutator().add(5);
		ol.mutator().add(50);
		ol.mutator().add(51);
		ol.mutator().add(52);
		ol.mutator().add(53);
		ol.mutator().add(54);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);
		fol.addObserver(observer);

		ol.mutator().set(6, 6);
		
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
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(50);
		ol.mutator().add(2);
		ol.mutator().add(51);
		ol.mutator().add(3);
		ol.mutator().add(52);
		ol.mutator().add(4);
		ol.mutator().add(53);
		ol.mutator().add(5);
		ol.mutator().add(54);
		ol.mutator().add(56);
		ol.mutator().add(57);
		ol.mutator().add(58);
		ol.mutator().add(59);
		ol.mutator().add(60);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);
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

		ol.mutator().set(2, newValues);
		
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
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(50);
		ol.mutator().add(2);
		ol.mutator().add(51);
		ol.mutator().add(3);
		ol.mutator().add(52);
		ol.mutator().add(4);
		ol.mutator().add(53);
		ol.mutator().add(5);
		ol.mutator().add(54);
		ol.mutator().add(56);
		ol.mutator().add(57);
		ol.mutator().add(58);
		ol.mutator().add(59);
		ol.mutator().add(60);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1, lock);
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

		ol.mutator().set(2, newValues);
		
		assertEquals(3, fol.getSize());
		List<Object> values = new ArrayList<>();
		values.add(Integer.valueOf(1));
		values.add(Integer.valueOf(7));
		values.add(Integer.valueOf(12));
		assertContainsAllItems(fol, values);
	}

	private static <T> void assertContainsAllItems(IReadOnlyObservableList<T> list, List<Object> items) {
		List<Object> copy = new ArrayList<>(items);
		
		for (int i = 0; i < list.getSize(); ++i) {
			assertTrue(copy.contains(list.getAt(i)));
			copy.remove(list.getAt(i));
		}
		assertEquals(items.size(), list.getSize());
		assertTrue(copy.isEmpty());
	}
}
