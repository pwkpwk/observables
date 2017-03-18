package com.ambientbytes.observables;

public interface IMutableList<T> extends IReadOnlyList<T> {
	void insert(int index, T value);
	void remove(int index, int count);
	void clear();
	void move(int startIndex, int newIndex, int count);
}
