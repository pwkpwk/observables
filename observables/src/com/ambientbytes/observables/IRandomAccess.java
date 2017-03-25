package com.ambientbytes.observables;

/**
 * Simple random access collection with basic access to elements with zero-based indexes.
 * @author Pavel Karpenko
 *
 * @param <T>
 */
interface IRandomAccess<T> {
	/**
	 * Get the length of the collection.
	 * @return number of elements in the collection.
	 */
	int size();
	
	/**
	 * Get the element at the specified zero-based index.
	 * @param index zero-based index of the element to retrieve.
	 * @return
	 */
	T get(int index);
}
