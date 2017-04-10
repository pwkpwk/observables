package com.ambientbytes.observables;

/**
 * @author Pavel Karpenko
 */

public final class MergingList<T> implements IObservableListContainer<T> {

    private final MergingReadOnlyObservableList<T> list;

    MergingList(IReadWriteMonitor monitor) {
        this.list = new MergingReadOnlyObservableList<>(monitor);
    }

    @Override
    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IReadOnlyObservableListSet<T> lists() {
        return list;
    }
}
