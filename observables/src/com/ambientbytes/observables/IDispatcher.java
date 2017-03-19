package com.ambientbytes.observables;

/**
 * Dispatcher of action objects. Dispatched actions are typically executed on a dedicated
 * thread or a thread pool after they've been dispatched.
 * @author Pavel Karpenko
 *
 */
public interface IDispatcher {
	/**
	 * Dispatch an action - defer execution of IAction.execute().
	 * @param action action to be dispatched.
	 */
	void dispatch(IAction action);
}
