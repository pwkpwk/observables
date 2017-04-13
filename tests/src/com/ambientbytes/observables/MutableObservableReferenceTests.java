package com.ambientbytes.observables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MutableObservableReferenceTests {

    @Mock IReferenceListener<Object> mockListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newMutableObservableReferenceCorrectSetup() {
        Object value = new Object();
        MutableObservableReference<Object> ref = new MutableObservableReference<Object>(value, mock(IReadWriteMonitor.class));

        assertSame(value, ref.getValue());
    }

    @Test
    public void changeValueWriteLock() {
        IReadWriteMonitor monitor = mock(IReadWriteMonitor.class);
        IResource wLock = mock(IResource.class);
        IResource rLock = mock(IResource.class);
        when(monitor.acquireWrite()).thenReturn(wLock);
        when(monitor.acquireRead()).thenReturn(rLock);
        Object value = new Object();
        Object newValue = new Object();
        MutableObservableReference<Object> ref = new MutableObservableReference<Object>(value, monitor);

        ref.setValue(newValue);

        verify(monitor, times(1)).acquireWrite();
        verify(wLock, times(1)).release();
    }

    @Test
    public void changeValueChanges() {
        IReadWriteMonitor monitor = mock(IReadWriteMonitor.class);
        IResource wLock = mock(IResource.class);
        IResource rLock = mock(IResource.class);
        when(monitor.acquireWrite()).thenReturn(wLock);
        when(monitor.acquireRead()).thenReturn(rLock);
        Object value = new Object();
        Object newValue = new Object();
        MutableObservableReference<Object> ref = new MutableObservableReference<Object>(value, monitor);

        ref.setValue(newValue);

        assertSame(newValue, ref.getValue());
    }

    @Test
    public void changeValueReportsChange() {
        IReadWriteMonitor monitor = mock(IReadWriteMonitor.class);
        IResource wLock = mock(IResource.class);
        IResource rLock = mock(IResource.class);
        when(monitor.acquireWrite()).thenReturn(wLock);
        when(monitor.acquireRead()).thenReturn(rLock);
        Object value = new Object();
        Object newValue = new Object();
        MutableObservableReference<Object> ref = new MutableObservableReference<Object>(value, monitor);
        ref.addListener(mockListener);

        ref.setValue(newValue);

        verify(mockListener, times(1)).changed(eq(ref), eq(value));
    }

    @Test
    public void removeListenerNoChangeReports() {
        IReadWriteMonitor monitor = mock(IReadWriteMonitor.class);
        IResource wLock = mock(IResource.class);
        IResource rLock = mock(IResource.class);
        when(monitor.acquireWrite()).thenReturn(wLock);
        when(monitor.acquireRead()).thenReturn(rLock);
        Object value = new Object();
        Object newValue = new Object();
        MutableObservableReference<Object> ref = new MutableObservableReference<Object>(value, monitor);
        ref.addListener(mockListener);
        ref.removeListener(mockListener);

        ref.setValue(newValue);

        verify(mockListener, never()).changed(any(IObservableReference.class), any());
    }
}
