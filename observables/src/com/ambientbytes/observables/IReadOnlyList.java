package com.ambientbytes.observables;

public interface IReadOnlyList<T> {
	T getAt(int index);
	int getSize();
}
