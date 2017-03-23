package com.ambientbytes.observables;

/**
 * Mutable collection of read-only observable lists.
 * @author Pavel Karpenko
 *
 * @param <T> item type of all lists in the collection.
 */
public interface IReadOnlyObservableListSet<T> {
	/**
	 * Add a new unique observable list to the set.
	 * Trying to add the same list two times throws a runtime exception.
	 * @param list observable list to be added to the set.
	 */
	void add(IReadOnlyObservableList<T> list);
	
	/**
	 * Remove a list from the set. If the list is not in the set, method does nothing.
	 * @param list list to be removed from the set.
	 */
	void remove(IReadOnlyObservableList<T> list);
}
