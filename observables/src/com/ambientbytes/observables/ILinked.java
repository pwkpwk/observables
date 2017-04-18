package com.ambientbytes.observables;

/**
 * Abstraction of an object that can be unlinked from some dependency object(s).
 * @author Pavel Karpenko
 *
 */
public interface ILinked {
	/**
	 * Unlink the object from its dependencies.
	 */
	void unlink();
}
