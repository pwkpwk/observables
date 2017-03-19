package com.ambientbytes.observables;

import java.util.Collection;

/**
 * Interface of a mutator of an observable list.
 * Mutations are separate from the data access interface.
 * @author Pavel Karpenko
 *
 * @param <T> type of items of the list.
 */
public interface IListMutator<T> {
	/**
	 * Append a new item at the end of the list.
	 * @param value item to be added to the list.
	 */
	void add(T value);
	
	/**
	 * Insert a new item at the specified location and push items from the location upwards.
	 * @param index index where the new item will appear. To add an item at the end of the list,
	 * specify the size of the list, or call add() instead. If the index is negative or beyoud the
	 * size of the list, the list throws an exception.
	 * @param value item to be added to the list.
	 */
	void insert(int index, T value);
	
	/**
	 * Remove a range of items from the list.
	 * @param index zero-based index of the first item to be removed.
	 * @param count number of items to remove.
	 * @return number of removed items; fewer items can be removed than requested. 
	 */
	int remove(int index, int count);
	
	/**
	 * Remove all items from the list.
	 */
	void clear();
	
	/**
	 * Move a rage of items in the list.
	 * @param startIndex index of the first item in the moved range.
	 * @param newIndex index where items must be moved.
	 * @param count number of items to move.
	 */
	void move(int startIndex, int newIndex, int count);

	/**
	 * Reset contents of the collection with new items.
	 * @param newItems new contents of the list.
	 */
	void reset(Collection<T> newItems);
}
