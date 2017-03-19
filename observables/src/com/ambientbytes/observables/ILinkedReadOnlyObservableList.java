package com.ambientbytes.observables;

public interface ILinkedReadOnlyObservableList<T> extends IReadOnlyObservableList<T> {
	/**
	 * Unlink from the linked observable list - stop observing and clear the all references.
	 */
	void unlink();
}
