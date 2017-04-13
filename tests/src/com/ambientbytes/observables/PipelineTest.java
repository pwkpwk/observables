package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PipelineTest {
		
	private static class TestModel {
		private int value;
		
		public TestModel(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	private static class TestViewModel implements IMutableObject {

		private final IReadWriteMonitor monitor;
		private Collection<IObjectMutationObserver> observers = new HashSet<>();
		private final TestModel model;
		private final IDispatcher dispatcher;
		private int value;
		
		public TestViewModel(IReadWriteMonitor monitor, TestModel model, IDispatcher dispatcher) {
			this.monitor = monitor;
			this.model = model;
			this.dispatcher = dispatcher;
			this.value = model.getValue();
		}
		
		public int getValue() {
			return value;
		}

		@Override
		public void addObserver(IObjectMutationObserver observer) {
			final IResource res = monitor.acquireWrite();
			
			try {
				observers.add(observer);
			} finally {
				res.release();
			}
		}

		@Override
		public void removeObserver(IObjectMutationObserver observer) {
			final IResource res = monitor.acquireWrite();
			
			try {
				observers.remove(observer);
			} finally {
				res.release();
			}
		}
		
		private void setValue(int newValue) {
			if (value != newValue) {
				value = newValue;
				notifyMutated();
			}
		}
		
		private IObjectMutationObserver[] makeInvocationList() {
			final IResource lock = monitor.acquireRead();
			IObjectMutationObserver[] invocationList;

			try {
				invocationList = observers.toArray(new IObjectMutationObserver[observers.size()]);
			} finally {
				lock.release();
			}
			
			return invocationList;
		}
		
		private void notifyMutated() {
			for (IObjectMutationObserver observer : makeInvocationList()) {
				observer.mutated();
			}
		}
	}
	
	private static class ModelFilter implements IItemFilter<TestModel> {
		@Override
		public boolean isIn(TestModel item) {
			return 0 == item.getValue() % 2;
		}
	}
	
	private static class ModelMapper implements IItemMapper<TestModel, TestViewModel> {
		
		private final IReadWriteMonitor monitor;
		private final IDispatcher dispatcher;
		
		public ModelMapper(IReadWriteMonitor monitor, IDispatcher dispatcher) {
			this.monitor = monitor;
			this.dispatcher = dispatcher;
		}

		@Override
		public TestViewModel map(TestModel item) {
			return new TestViewModel(monitor, item, dispatcher);
		}
	}
	
	private static class ViewModelOrder implements IItemsOrder<TestViewModel> {
		@Override
		public boolean isLess(TestViewModel lesser, TestViewModel greater) {
			return lesser.getValue() < greater.getValue();
		}
	}
	
	private static class ViewModelReverseOrder implements IItemsOrder<TestViewModel> {
		@Override
		public boolean isLess(TestViewModel lesser, TestViewModel greater) {
			return greater.getValue() < lesser.getValue();
		}
	}
	
	@Captor
	ArgumentCaptor<IAction> actionCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() {
		IDispatcher dispatcher = mock(IDispatcher.class);
		IReadWriteMonitor monitor = LockTool.createReadWriteMonitor(new ReentrantReadWriteLock());
		ObservableList<TestModel> models = ObservableCollections.createObservableList(monitor);
		IListMutator<TestModel> mutator = models.mutator();

		IReadOnlyObservableList<TestViewModel> list = ListBuilder.forSource(models.list(), monitor)
				.filter(new ImmutableObservableReference<IItemFilter<TestModel>>(new ModelFilter()))
				.map(new ModelMapper(monitor, dispatcher))
				.order(new ImmutableObservableReference<IItemsOrder<TestViewModel>>(new ViewModelOrder()))
				.dispatch(dispatcher)
				.build();

		for (int i = 1; i <= 10000; ++i) {
			mutator.add(new TestModel(i));
		}
		
		verify(dispatcher, atLeast(1)).dispatch(actionCaptor.capture());
		for (IAction action : actionCaptor.getAllValues()) {
			action.execute();
		}
		
		assertEquals(5000, list.getSize());
	}

}
