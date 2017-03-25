package com.ambientbytes.observables;

import java.util.List;

public class ListRandomAccess<T> implements IRandomAccess<T> {
	
	private final List<T> data;
	
	public ListRandomAccess(List<T> data) {
		this.data = data;
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public T get(int index) {
		return data.get(index);
	}
}
