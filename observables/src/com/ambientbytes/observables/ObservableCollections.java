package com.ambientbytes.observables;

public final class ObservableCollections {
	public static <T> IMutableList<T> createMutableObservableList() {
		return new MutableObservableList<T>();
	}
}
