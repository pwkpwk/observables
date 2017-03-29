package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by pakarpen on 3/29/17.
 */

public final class MergingObservableList<T> {

    private final MergingReadOnlyObservableList<T> list;

    public MergingObservableList(ReadWriteLock lock) {
        this.list = new MergingReadOnlyObservableList<>(lock);
    }

    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IReadOnlyObservableListSet<T> lists() {
        return list;
    }
}
