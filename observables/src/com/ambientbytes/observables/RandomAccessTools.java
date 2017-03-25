package com.ambientbytes.observables;

class RandomAccessTools {
	public static <T> int indexOfFirstGreaterOrEqual(
			IRandomAccess<T> data,
			IItemsOrder<T> order,
			T value) {
		//
		// Return index of the first item that is greater or equal than the specified item
		// according to the set order.
		// A new item may be inserted at the returned index.
		//
		int left = -1;
		int right = data.size();
		
		while (left + 1 != right) {
			int middle = left + (right - left) / 2;
			
			if (order.isLess(data.get(middle), value)) {
				left = middle;
			} else {
				right = middle;
			}
		}
		
		return right;
	}
}
