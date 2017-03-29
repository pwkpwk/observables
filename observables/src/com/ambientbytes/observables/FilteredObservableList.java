package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by pakarpen on 3/29/17.
 */

public final class FilteredObservableList<T> {

    private final FilteringReadOnlyObservableList<T> list;

    public FilteredObservableList(IReadOnlyObservableList<T> source, IItemFilter<T> filter, ReadWriteLock lock) {
        this.list = new FilteringReadOnlyObservableList<>(source, filter, lock);
    }

    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IItemFilterContainer<T> filters() {
        return list;
    }
}
