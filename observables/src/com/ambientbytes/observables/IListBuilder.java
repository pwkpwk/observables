package com.ambientbytes.observables;

/**
 * Interface of an observable list builder.
 * @author Pavel Karpenko
 */

public interface IListBuilder<T> {
    IListBuilder<T> dispatch(IDispatcher dispatcher);
    IListBuilder<T> filter(IObservableReference<IItemFilter<T>> filter);
    IListBuilder<T> order(IObservableReference<IItemsOrder<T>> order);
    <TMapped> IListBuilder<TMapped> map(IItemMapper<T, TMapped> mapper);
    IReadOnlyObservableList<T> build();
}
