package com.ambientbytes.observables;

import java.util.concurrent.locks.ReadWriteLock;

public final class ObservableCollections {

	/**
	 * Create a new observable list with a mutator without a thread lock.
	 * @return A new ObservableList<T> with non-null list and mutator properties.
	 */
	public static <T> ObservableList<T> createObservableList() {
		MutableObservableList<T> list = new MutableObservableList<>(new DummyReadWriteLock());
		return new ObservableList<T>(list, list.getMutator());
	}
	
	/**
	 * Create a new observable list with a mutator with a thread lock. The list locks
	 * the write lock of the supplied ReadWriteLock object for each mutation, and emits
	 * all mutation events while holding the write lock.
	 * @return A new ObservableList<T> with non-null list and mutator properties.
	 */
	public static <T> ObservableList<T> createObservableList(ReadWriteLock lock) {
		MutableObservableList<T> list = new MutableObservableList<>(lock);
		return new ObservableList<T>(list, list.getMutator());
	}

	public static <T> OrderedObservableList<T> createOrderedObservableList(IReadOnlyObservableList<T> source, IItemsOrder<T> order, ReadWriteLock lock) {
		return new OrderedObservableList<>(new OrderingReadOnlyObservableList<>(source, order, lock));
	}
}
