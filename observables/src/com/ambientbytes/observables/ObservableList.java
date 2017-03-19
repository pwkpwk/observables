package com.ambientbytes.observables;

public final class ObservableList<T> {
	private final IReadOnlyObservableList<T> list;
	private final IListMutator<T> mutator;
	
	public ObservableList(final IReadOnlyObservableList<T> list, final IListMutator<T> mutator) {
		this.list = list;
		this.mutator = mutator;
	}
	
	public IReadOnlyObservableList<T> list() {
		return list;
	}
	
	public IListMutator<T> mutator() {
		return mutator;
	}
}
