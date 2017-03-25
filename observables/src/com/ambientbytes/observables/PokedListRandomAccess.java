package com.ambientbytes.observables;

import java.util.List;

/**
 * Wrapper of a List object that ignores one item in the list.
 * @author Pavel Karpenko
 *
 * @param <T> type of items in the list.
 */
final class PokedListRandomAccess<T> implements IRandomAccess<T> {
	
	private final List<T> data;
	private final int holeIndex;
	
	public PokedListRandomAccess(List<T> data, int holeIndex) {
		if (holeIndex < 0 || holeIndex >= data.size()) {
			throw new IndexOutOfBoundsException();
		}
		
		this.data = data;
		this.holeIndex = holeIndex;
	}

	@Override
	public int size() {
		return data.size() - 1;
	}

	@Override
	public T get(int index) {
		return data.get(getTrueIndex(index));
	}

	private int getTrueIndex(int index) {
		return index < holeIndex ? index : index + 1;
	}
}
