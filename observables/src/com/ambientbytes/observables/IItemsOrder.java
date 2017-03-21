package com.ambientbytes.observables;

/**
 * Interface establishes order of items in observable lists.
 * @author Pavel Karpenko
 *
 * @param <T> type of ordered items.
 */
public interface IItemsOrder<T> {
	/**
	 * Test if the lesser item is lass than the greater item.
	 * @param lesser item to test for being lesser than the other item.
	 * @param greater item to test for being greater than the other item.
	 * @return true if lesser is less than greater.
	 */
	boolean isLess(T lesser, T greater);
}
