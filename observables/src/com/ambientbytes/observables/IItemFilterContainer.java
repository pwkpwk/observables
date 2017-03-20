package com.ambientbytes.observables;

public interface IItemFilterContainer<T> {
	IItemFilter<T> getFilter();
	void setFilter(IItemFilter<T> filter);
}
