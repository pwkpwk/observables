package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LinkedReadOnlyObservableListTests {
	
	private static final class TestList extends LinkedReadOnlyObservableList<Integer> {

		protected TestList(IReadOnlyObservableList<Integer> source, IReadWriteMonitor monitor) {
			super(source, monitor);
		}

		@Override
		public Integer getAt(int index) {
			return null;
		}

		@Override
		public int getSize() {
			return 0;
		}

		@Override
		protected void onAdded(IReadOnlyObservableList<Integer> source, int startIndex, int count) {
		}

		@Override
		protected void onChanging(IReadOnlyObservableList<Integer> source, int startIndex, int count) {
		}

		@Override
		protected void onChanged(IReadOnlyObservableList<Integer> source, int startIndex, int count) {
		}

		@Override
		protected void onRemoving(IReadOnlyObservableList<Integer> source, int startIndex, int count) {
		}

		@Override
		protected void onRemoved(IReadOnlyObservableList<Integer> source, int startIndex, int count) {
		}

		@Override
		protected void onMoved(IReadOnlyObservableList<Integer> source, int oldStartIndex, int newStartIndex, int count) {
		}

		@Override
		protected void onResetting(IReadOnlyObservableList<Integer> source) {
		}

		@Override
		protected void onReset(IReadOnlyObservableList<Integer> source) {
		}
	}
	
	@Mock ILinkedReadOnlyObservableList<Integer> mockSource;	
	@Captor ArgumentCaptor<IListObserver> observer1;
	@Captor ArgumentCaptor<IListObserver> observer2;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void newAddsObserver() {
		new TestList(mockSource, new DummyReadWriteMonitor());
		
		verify(mockSource, times(1)).addObserver(any(IListObserver.class));
		verify(mockSource, never()).removeObserver(any(IListObserver.class));
	}

	@Test
	public void unlinkRemovesObserver() {
		TestList list = new TestList(mockSource, new DummyReadWriteMonitor());
		
		list.unlink();
		
		verify(mockSource, times(1)).addObserver(observer1.capture());
		verify(mockSource, times(1)).removeObserver(observer2.capture());
		assertNotNull(observer1.getValue());
		assertSame(observer1.getValue(), observer2.getValue());
	}

}
