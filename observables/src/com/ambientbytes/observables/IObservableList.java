package com.ambientbytes.observables;

public interface IObservableList<T> {
	/**
	 * Add a new unique non-null observer. An observer may be added to the observable list only once.
	 * An attempt to add an observer again must throw an exception.
	 * @param observer new observer that will be receive changes made to the list.
	 */
	void addObserver(IListObserver<T> observer);
	
	/**
	 * Remove a registered observer. Each change of the list is reported to all observers
	 * registered at the moment of change.
	 * @param observer observer to be removed.
	 */
	void removeObserver(IListObserver<T> observer);
}
