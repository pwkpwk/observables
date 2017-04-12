package com.ambientbytes.observables;

/**
 * An observable reference
 * @author Pavel Karpenko
 */

public interface IObservableReference<T> {
    T getValue();

    void addListener(IReferenceListener<T> listener);
    void removeListener(IReferenceListener<T> listener);
}
