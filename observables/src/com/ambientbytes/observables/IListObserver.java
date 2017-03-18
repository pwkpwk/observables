package com.ambientbytes.observables;

import java.util.Collection;

public interface IListObserver<T> {
	/**
	 * Called after new items have been added to the observed list.
	 * @param startIndex zero-based index of the first added item.
	 * @param count number of added items.
	 */
	void added(int startIndex, int count);
	
	/**
	 * Called after items have been removed from the observed list.
	 * @param startIndex zero-based index of the first removed item.
	 * @param items removed items.
	 */
	void removed(int startIndex, Collection<T> items);

	/**
	 * Called after items have been moved in the observed list.
	 * @param oldStartIndex old zero-based index of the first moved item.
	 * @param newStartIndex new zero-based index of the first moved item.
	 * @param count number of moved items.
	 */
	void moved(int oldStartIndex, int newStartIndex, int count);

	/**
	 * Contents of the observed list have been reset.
	 * @param oldItems contents of the list before it's been reset.
	 */
	void reset(Collection<T> oldItems);
}
