package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListBuilderTests {
	
	@Mock IReadOnlyObservableList<Integer> source;
	@Mock IReadWriteMonitor monitor;
	@Mock IResource rLock;
	@Mock IResource wLock;
	@Mock IItemFilter<Integer> filter;
	@Mock IDispatcher dispatcher;
	@Mock IItemMapper<Integer, Integer> mapper;
	@Mock IItemsOrder<Integer> order;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(monitor.acquireRead()).thenReturn(rLock);
		when(monitor.acquireWrite()).thenReturn(wLock);
		when(filter.isIn(any(Integer.class))).thenReturn(true);
		when(mapper.map(any(Integer.class))).thenReturn(1);
	}

	@Test
	public void singleSourceAddFilterUnlinkUnlinks() {
		Trigger trigger = new Trigger(monitor);
		IReadOnlyObservableList<Integer> list = ListBuilder
				.<Integer>unlinker(trigger)
				.source(source, monitor)
				.filter(new ImmutableObservableReference<>(filter))
				.build();

		verify(source, times(1)).addObserver(any(IListObserver.class));
		verify(source, never()).removeObserver(any(IListObserver.class));
		trigger.trigger();
		
		verify(source, times(1)).removeObserver(any(IListObserver.class));
	}

	@Test
	public void mergedSourceAddFilterUnlinkUnlinks() {
		Trigger trigger = new Trigger(monitor);
		IReadOnlyObservableList<Integer> list = ListBuilder
				.<Integer>unlinker(trigger)
				.merge(new MutableListSet<>(monitor, source), monitor)
				.filter(new ImmutableObservableReference<>(filter))
				.build();

		verify(source, times(1)).addObserver(any(IListObserver.class));
		verify(source, never()).removeObserver(any(IListObserver.class));
		trigger.trigger();
		
		verify(source, times(1)).removeObserver(any(IListObserver.class));
	}

	@Test
	public void singleSourceAddDispatcherUnlinkUnlinks() {
		Trigger trigger = new Trigger(monitor);
		IReadOnlyObservableList<Integer> list = ListBuilder
				.<Integer>unlinker(trigger)
				.source(source, monitor)
				.dispatch(dispatcher)
				.build();

		verify(source, times(1)).addObserver(any(IListObserver.class));
		verify(source, never()).removeObserver(any(IListObserver.class));
		trigger.trigger();
		
		verify(source, times(1)).removeObserver(any(IListObserver.class));
	}

	@Test
	public void singleSourceAddMapperUnlinkUnlinks() {
		Trigger trigger = new Trigger(monitor);
		IReadOnlyObservableList<Integer> list = ListBuilder
				.<Integer>unlinker(trigger)
				.source(source, monitor)
				.map(mapper).build();

		verify(source, times(1)).addObserver(any(IListObserver.class));
		verify(source, never()).removeObserver(any(IListObserver.class));
		trigger.trigger();
		
		verify(source, times(1)).removeObserver(any(IListObserver.class));
	}

	@Test
	public void singleSourceAddOrderUnlinkUnlinks() {
		Trigger trigger = new Trigger(monitor);
		IReadOnlyObservableList<Integer> list = ListBuilder
				.<Integer>unlinker(trigger)
				.source(source, monitor)
				.order(new ImmutableObservableReference<>(order))
				.build();

		verify(source, times(1)).addObserver(any(IListObserver.class));
		verify(source, never()).removeObserver(any(IListObserver.class));
		trigger.trigger();
		
		verify(source, times(1)).removeObserver(any(IListObserver.class));
	}

}
