package com.ambientbytes.observables;

class ArrayListEx<E> extends java.util.ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4801793454171801218L;

	public ArrayListEx() {
	}
	
	public ArrayListEx(int capacity) {
		super(capacity);
	}
	
	public void remove(int start, int length) {
		if (start < 0 || length < 0 || start + length > size()) {
			throw new IndexOutOfBoundsException();
		}
		
		removeRange(start, start + length);
	}
	
	public void move(final int source, final int destination, final int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length may not be negative");
		}

		if (source < 0 || destination < 0 || source + length > size() || destination + length > size()) {
			throw new IndexOutOfBoundsException();
		}

		if (source != destination && length > 0) {
			final int low, pivot, high;
			
			if (source < destination) {
				low = source;
				pivot = source + length;
				high = destination + length;
			} else {
				low = destination;
				pivot = source;
				high = pivot + length;
			}
			// To shift the source range we find a pivoting point and rotate the part of the list
			// that cover the entire range affected by the move three times - left and right of the pivoting point
			// and then the entire range.
			reverseRange(low, pivot);
			reverseRange(pivot, high);
			reverseRange(low, high);
		}
	}
	
	private final void reverseRange(int low, int high) {
		int l = low;
		int h = high - 1;
		
		while (l < h) {
			E tmp = get(l);
			set(l, get(h));
			set(h,  tmp);
			l++;
			h--;
		}
	}
}
