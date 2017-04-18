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
	 * Insert a new item at the specified location and push existing items from that location upwards.
	 * @param index index where the new item will appear. To add an item at the end of the list,
	 * specify the size of the list, or call add() instead. If the index is negative or beyoud the
	 * size of the list, the list throws an exception.
	 * @param value item to be added to the list.
	 */
	void add(int index, T value);

	/**
	 * Insert a collection of new items at the specified location and push existing items from that location upwards.
	 * @param index index where the first inserted item will appear, followed by the rest of the new items.
	 * @param values iterable collection of items to be added to the list.
	 */
	void add(int index, Collection<T> values);

	/**
	 * Change an item at the specified index.
	 * @param index index of the item to be changed.
	 * @param value new value for the item.
	 */
	void set(int index, T value);
	
	/**
	 * Change a range of items in the list starting at the specified index.
	 * @param index index of the first item to be changed.
	 * @param values iterable collection of items to replace items in the list.
	 * The method throws an exception if the collection does not fit in the list.
	 */
	void set(int index, Collection<T> values);
	
	/**
	 * Remove a range of items from the list.
	 * @param index zero-based index of the first item to be removed.
	 * @param count number of items to remove.
	 * @return number of removed items; fewer items can be removed than requested. 
	 */
	void remove(int index, int count);
	
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
