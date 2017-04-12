package com.ambientbytes.observables;

public final class ObservableCollections {

	/**
	 * Create a new observable list with a mutator without a thread lock.
	 * @return A new ObservableList<T> with non-null list and mutator properties.
	 */
	public static <T> ObservableList<T> createObservableList() {
		MutableObservableList<T> list = new MutableObservableList<>(new DummyReadWriteMonitor());
		return new ObservableList<>(list, list.getMutator());
	}
	
	/**
	 * Create a new observable list with a mutator with a thread lock. The list locks
	 * the write lock of the supplied ReadWriteLock object for each mutation, and emits
	 * all mutation events while holding the write lock.
	 * @return A new ObservableList<T> with non-null list and mutator properties.
	 */
	public static <T> ObservableList<T> createObservableList(IReadWriteMonitor monitor) {
		MutableObservableList<T> list = new MutableObservableList<>(monitor);
		return new ObservableList<>(list, list.getMutator());
	}

	public static <T> MergingList<T> createMergingObservableList(IReadWriteMonitor monitor) {
        return new MergingList<>(monitor);
    }
}
