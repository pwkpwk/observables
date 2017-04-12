package com.ambientbytes.observables;

/**
 * Immutable implementation of IObservableReference.
 * @author Pavel Karpenko
 */

public final class ImmutableObservableReference<T> implements IObservableReference<T> {

    private final T value;

    public ImmutableObservableReference(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void addListener(IReferenceListener<T> listener) {}

    @Override
    public void removeListener(IReferenceListener<T> listener) {}
}
