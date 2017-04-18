package com.ambientbytes.observables;

/**
 * Listener of requests from a list mutator.
 * @param <T> type of items in the list changed by the corresponding mutator.
 * @author Pavel Karpenko
 *
 */
public interface IListMutatorListener<T> {
	/**
	 * Add a new unique non-null listener object.
	 * @param listener unique non-null listener object to be added.
	 */
	void addListener(IListMutator<T> listener);

	/**
	 * Remove a listener object.
	 * @param listener listener to be removed.
	 */
	void removeListener(IListMutator<T> listener);
}
