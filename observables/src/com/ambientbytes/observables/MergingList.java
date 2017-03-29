package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author Pavel Karpenko
 */

public final class MergingList<T> implements IObservableListContainer<T> {

    private final MergingReadOnlyObservableList<T> list;

    public MergingList(ReadWriteLock lock) {
        this.list = new MergingReadOnlyObservableList<>(lock);
    }

    @Override
    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IReadOnlyObservableListSet<T> lists() {
        return list;
    }
}
