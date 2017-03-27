package com.ambientbytes.observables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

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

		private final ReadWriteLock lock;
		private Collection<IObjectMutationObserver> observers = new HashSet<>();
		private final TestModel model;
		private final IDispatcher dispatcher;
		private int value;
		
		public TestViewModel(ReadWriteLock lock, TestModel model, IDispatcher dispatcher) {
			this.lock = lock;
			this.model = model;
			this.dispatcher = dispatcher;
			this.value = model.getValue();
		}
		
		public int getValue() {
			return value;
		}

		@Override
		public void addObserver(IObjectMutationObserver observer) {
			final IResource res = LockTool.acquireWriteLock(lock);
			
			try {
				observers.add(observer);
			} finally {
				res.release();
			}
		}

		@Override
		public void removeObserver(IObjectMutationObserver observer) {
			final IResource res = LockTool.acquireWriteLock(lock);
			
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
			final Lock l = lock.readLock();
			IObjectMutationObserver[] invocationList;
			
			l.lock();
			
			try {
				invocationList = observers.toArray(new IObjectMutationObserver[observers.size()]);
			} finally {
				l.unlock();
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
		
		private final ReadWriteLock lock;
		private final IDispatcher dispatcher;
		
		public ModelMapper(ReadWriteLock lock, IDispatcher dispatcher) {
			this.lock = lock;
			this.dispatcher = dispatcher;
		}

		@Override
		public TestViewModel map(TestModel item) {
			return new TestViewModel(lock, item, dispatcher);
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
		ReadWriteLock lock = new ReentrantReadWriteLock(); 
		ObservableList<TestModel> models = ObservableCollections.createObservableList(lock);
		IListMutator<TestModel> mutator = models.mutator();
		
		ILinkedReadOnlyObservableList<TestModel> filtered =
				new FilteringReadOnlyObservableList<>(models.list(), new ModelFilter(), lock);
		
		ILinkedReadOnlyObservableList<TestViewModel> mapped =
				new MappingReadOnlyObservableList<TestModel, TestViewModel>(filtered, new ModelMapper(lock, dispatcher));
		
		ILinkedReadOnlyObservableList<TestViewModel> ordering =
				new OrderingReadOnlyObservableList<>(mapped, new ViewModelOrder(), lock);
		
		ILinkedReadOnlyObservableList<TestViewModel> list =
				new DispatchingObservableList<>(ordering, dispatcher, lock);

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
