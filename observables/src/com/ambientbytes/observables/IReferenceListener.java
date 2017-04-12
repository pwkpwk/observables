package com.ambientbytes.observables;

/**
 * Listener of events emitted by IObservableReference.
 * @author Pavel Karpenko
 */

public interface IReferenceListener<T> {
    void changed(IObservableReference<T> sender, T oldValue);
}
