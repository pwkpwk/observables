package com.ambientbytes.observables;

/**
 * Container of a filter predicate object. Filter predicates are used to filter out items from
 * filtered observable lists.
 * @param <T> type of objects passed to the filter.
 */
public interface IItemFilterContainer<T> {
	/**
	 * Get the current filter object.
	 * @return the current filter object.
	 */
	IItemFilter<T> getFilter();

    /**
     * Apply a new filter object.
     * @param filter new filter object.
     */
	void setFilter(IItemFilter<T> filter);
}
