package com.ambientbytes.observables;

/**
 * Created by pakarpen on 3/29/17.
 */

public interface IObservableListContainer<T> {
    IReadOnlyObservableList<T> list();
}
