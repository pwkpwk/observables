package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TriggerTests {
	
	@Mock ITriggerListener listener;
	@Mock IReadWriteMonitor monitor;
	@Mock IResource rLock;
	@Mock IResource wLock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(monitor.acquireRead()).thenReturn(rLock);
		when(monitor.acquireWrite()).thenReturn(wLock);
	}

	@Test
	public void triggerTriggered() {
		Trigger trigger = new Trigger(monitor);
		trigger.addListener(listener);
		
		trigger.trigger();
		
		verify(listener, times(1)).triggered(trigger);
	}

	@Test
	public void removeListenerTriggerNotTriggered() {
		Trigger trigger = new Trigger(monitor);
		trigger.addListener(listener);
		trigger.removeListener(listener);
		
		trigger.trigger();
		
		verify(listener, never()).triggered(trigger);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addListenerTwiceThrows() {
		Trigger trigger = new Trigger(monitor);
		
		trigger.addListener(listener);
		trigger.addListener(listener);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addNullListenerThrows() {
		Trigger trigger = new Trigger(monitor);
		
		trigger.addListener(null);
	}
	
	@Test
	public void addListenerAcquiresWriteLock() {
		Trigger trigger = new Trigger(monitor);
		
		trigger.addListener(listener);
		
		verify(monitor, never()).acquireRead();
		verify(wLock, times(1)).release();
	}

}
