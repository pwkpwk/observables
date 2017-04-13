package com.ambientbytes.observables;

/**
 * Listener of events emitted by a trigger.
 * @author Pavel Karpenko
 *
 */
public interface ITriggerListener {
	void triggered(ITrigger sender);
}
