package com.ambientbytes.observables;

/**
 * Filter of items for observable collections that filter some items out.
 * @author Pavel Karpenko
 *
 * @param <T> item type.
 */
public interface IItemFilter<T> {
	/**
	 * Filtering predicate.
	 * @param item item tested for complying with the filter.
	 * @return true if the item passes through the filter; otherwise, false.
	 */
	boolean isIn(T item);
}
