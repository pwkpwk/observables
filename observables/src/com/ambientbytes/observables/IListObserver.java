package com.ambientbytes.observables;

/**
 * Observer of an observable list.
 * @author Pavel Karpenko
 */
public interface IListObserver {
	/**
	 * Called after new items have been added to the observed list.
	 * @param startIndex zero-based index of the first added item.
	 * @param count number of added items.
	 */
	void added(int startIndex, int count);

	/**
	 * Called before a range of items in the list will be changed.
	 * @param startIndex zero-based index of the first changed item.
	 * @param count number of items to be changed.
	 */
	void changing(int startIndex, int count);

	/**
	 * Called after a range of items in the list has been changed.
	 * @param startIndex zero-based index of the first changed item.
	 * @param count number of changed items.
	 */
	void changed(int startIndex, int count);

	/**
	 * Called immediately before removing items from the observed list.
	 * @param startIndex zero-based index of the first item to be removed.
	 * @param count number of items to be removed.
	 */
	void removing(int startIndex, int count);
	
	/**
	 * Called after items have been removed from the observed list.
	 * @param startIndex zero-based index of the first removed item.
	 * @param count number of removed items.
	 */
	void removed(int startIndex, int count);

	/**
	 * Called after items have been moved in the observed list.
	 * @param oldStartIndex old zero-based index of the first moved item.
	 * @param newStartIndex new zero-based index of the first moved item.
	 * @param count number of moved items.
	 */
	void moved(int oldStartIndex, int newStartIndex, int count);

	/**
	 * Called before contents of the observed list will be completely replaced.
	 */
	void resetting();
	
	/**
	 * Called after contents of the observed list have been completely replaced.
	 */
	void reset();
}
