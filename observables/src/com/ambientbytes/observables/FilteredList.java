package com.ambientbytes.observables;

/**
 * Combination of a read-only observable list and a filter predicate that filters out items from the source list.
 * @author Pavel Karpenko
 */

public final class FilteredList<T> implements IObservableListContainer<T> {

    private final FilteringReadOnlyObservableList<T> list;

    FilteredList(IReadOnlyObservableList<T> source, IItemFilter<T> filter, IReadWriteMonitor monitor) {
        this.list = new FilteringReadOnlyObservableList<>(source, filter, monitor);
    }

    @Override
    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IItemFilterContainer<T> filter() {
        return list;
    }
}
