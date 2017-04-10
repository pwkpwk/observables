package com.ambientbytes.observables;

/**
 * @author Pavel Karpenko
 */

public final class OrderedList<T> implements IObservableListContainer<T> {

    private final OrderingReadOnlyObservableList<T> list;

    OrderedList(OrderingReadOnlyObservableList<T> list) {
        this.list = list;
    }

    @Override
    public IReadOnlyObservableList<T> list() {
        return list;
    }

    public IItemsOrderContainer<T> order() {
        return list;
    }
}
