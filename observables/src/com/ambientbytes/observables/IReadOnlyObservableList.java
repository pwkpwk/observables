package com.ambientbytes.observables;

/**
 * Read-only observable list.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in the list.
 */
public interface IReadOnlyObservableList<T> {
	/**
	 * Get an item at the specified index.
	 * @param index zero-based index of the item to retrieve. If the index is outside of the list,
	 * the method throws an exception.
	 * @return item at the specified index.
	 */
	T getAt(int index);
	
	/**
	 * Get the current size of the list.
	 * @return size of the list.
	 */
	int getSize();

	/**
	 * Add a new unique non-null observer. An observer may be added to the observable list only once.
	 * An attempt to add an observer again must throw an exception.
	 * @param observer new observer that will be receive changes made to the list.
	 */
	void addObserver(IListObserver observer);
	
	/**
	 * Remove a registered observer. Each change of the list is reported to all observers
	 * registered at the moment of change.
	 * @param observer observer to be removed.
	 */
	void removeObserver(IListObserver observer);
}
