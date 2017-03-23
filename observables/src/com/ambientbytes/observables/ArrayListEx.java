package com.ambientbytes.observables;

class ArrayListEx<E> extends java.util.ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4801793454171801218L;

	public ArrayListEx() {
	}
	
	public void remove(int start, int length) {
		removeRange(start, start + length);
	}
}
