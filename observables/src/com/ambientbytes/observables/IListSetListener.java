package com.ambientbytes.observables;

public interface IListSetListener<T> {
	void added(IListSet<T> source, IReadOnlyObservableList<T> list);
	void removed(IListSet<T> source, IReadOnlyObservableList<T> list);
}
