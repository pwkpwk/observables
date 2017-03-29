package com.ambientbytes.observables;

/**
 * @Author Pavel Karpenko
 */

public final class OrderedObservableList<T> {

    private final OrderingReadOnlyObservableList<T> list;

    OrderedObservableList(OrderingReadOnlyObservableList<T> list) {
        this.list = list;
    }

    public IItemsOrderContainer<T> order() {
        return list;
    }

    public IReadOnlyObservableList<T> list() {
        return list;
    }
}
