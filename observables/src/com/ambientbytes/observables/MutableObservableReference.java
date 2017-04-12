package com.ambientbytes.observables;

import java.util.Collection;
import java.util.HashSet;

/**
 * Locking implementation of IObservableReference.
 * @author Pavel Karpenko
 */

public final class MutableObservableReference<T> implements IObservableReference<T> {

    private final IReadWriteMonitor monitor;
    private final Collection<IReferenceListener<T>> listeners;
    private T value;

    public MutableObservableReference(T value, IReadWriteMonitor monitor) {
        this.monitor = monitor;
        this.listeners = new HashSet<>();
        this.value = value;
    }

    public void setValue(T newValue) {
        IResource lock = monitor.acquireWrite();

        try {
            if (value != newValue) {
                T oldValue = value;
                value = newValue;
                for (IReferenceListener<T> listener : listeners) {
                    listener.changed(this, oldValue);
                }
            }
        } finally {
            lock.release();
        }
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void addListener(IReferenceListener<T> listener) {
        IResource lock = monitor.acquireWrite();

        try {
            listeners.add(listener);
        } finally {
            lock.release();
        }
    }

    @Override
    public void removeListener(IReferenceListener<T> listener) {
        IResource lock = monitor.acquireWrite();

        try {
            listeners.remove(listener);
        } finally {
            lock.release();
        }
    }
}
