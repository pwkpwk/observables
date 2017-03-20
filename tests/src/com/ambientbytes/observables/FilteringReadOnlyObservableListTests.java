package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
					observer.mutated(this);
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
	
	@Mock
	IItemFilter<Integer> mockFilter1;
	@Mock
	IItemFilter<Integer> mockFilter2;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newListPermitAllAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

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
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

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
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

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
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

		assertEquals(0, fol.getSize());
		fol.setFilter(mockFilter2);
		verify(mockFilter2, times(3)).isIn(any(Integer.class));
		assertEquals(3, fol.getSize());
	}

	@Test
	public void permitAllAddItemsAllAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(true);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

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
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);
		ol.mutator().remove(2, 1);

		assertEquals(2, fol.getSize());
	}

	@Test
	public void permitNoneAddItemsNoneAdded() {
		when(mockFilter1.isIn(any(Integer.class))).thenReturn(false);
		ObservableList<Integer> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<Integer> fol = new FilteringReadOnlyObservableList<>(ol.list(), mockFilter1);

		ol.mutator().add(1);
		ol.mutator().add(2);
		ol.mutator().add(3);

		verify(mockFilter1, times(3)).isIn(any(Integer.class));
		assertEquals(0, fol.getSize());
	}
	
	@Test
	public void mutateToAllowedItemAppears() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter());
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
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter());
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
	public void unlinkNoMoreUpdates() {
		ObservableList<TestItem> ol = ObservableCollections.createObservableList();
		FilteringReadOnlyObservableList<TestItem> fol = new FilteringReadOnlyObservableList<>(ol.list(), new TestFilter());
		TestItem item;
		
		ol.mutator().add(new TestItem(1));
		ol.mutator().add(new TestItem(2));
		fol.unlink();
		ol.mutator().add(item = new TestItem(3));
		ol.mutator().add(new TestItem(12));
		
		assertEquals(2, fol.getSize());
		item.setValue(11);
		assertEquals(2, fol.getSize());
	}

}
