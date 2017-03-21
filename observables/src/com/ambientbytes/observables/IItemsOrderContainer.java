package com.ambientbytes.observables;

/**
 * Container that sorts items using an IItemsOrder object.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in the container.
 */
public interface IItemsOrderContainer<T> {
	/**
	 * Retrieve the current ordering object.
	 * @return current ordering object.
	 */
	IItemsOrder<T> getOrder();
	
	/**
	 * Apply new ordering object - change the order of items in the container.
	 * @param order new ordering object.
	 */
	void setOrder(IItemsOrder<T> order);
}
