package com.ambientbytes.observables;

/**
 * Observable set of read-only observable lists.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in lists in the set.
 */
public interface IListSet<T> extends Iterable<IReadOnlyObservableList<T>> {
	/**
	 * Add a new listener to receive notifications about changes of the set.
	 * @param listener listener to be added. An attempt to add the same listener again will throw a runtime exception.
	 */
	void addListener(IListSetListener<T> listener);
	
	/**
	 * Remove a listener.
	 * @param listener listener to be removed. If the listener hadn't been added to the set, method does nothing.
	 */
	void removeListener(IListSetListener<T> listener);
}
