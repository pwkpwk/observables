package com.ambientbytes.observables;

/**
 * Mapper between two types of items.
 * @author Pavel Karpenko
 *
 * @param <TSource> source item type.
 * @param <TMapped> mapped item type.
 */
public interface IItemMapper<TSource, TMapped> {
	/**
	 * Map an item of the source type to an item of the mapped type.
	 * @param item
	 * @return
	 */
	TMapped map(TSource item);
}
