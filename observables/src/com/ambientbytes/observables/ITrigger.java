package com.ambientbytes.observables;

/**
 * Interface of an object that can trigger a simple event.
 * @author Pavel Karpenko
 *
 */
public interface ITrigger {
	/**
	 * Add a new listener to the trigger. Listeners must be unique; an attempt to add
	 * the same listener object again will throw a runtime exception. 
	 * @param listener non-null listener to be added to the trigger.
	 */
	void addListener(ITriggerListener listener);

	/**
	 * Remove a listener from the trigger.
	 * If the listener hadn't been added to the trigger, method does nothing.
	 * @param listener listener to be removed from the trigger.
	 */
	void removeListener(ITriggerListener listener);
}
