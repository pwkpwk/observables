package com.ambientbytes.observables;

/**
 * Observer of mutations of IMutableObject.
 * @author Pavel Karpenko
 *
 */
public interface IObjectMutationObserver {
	/**
	 * Called when object has mutated.
	 */
	void mutated();
}
