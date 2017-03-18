package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class MutableObservableList<T> implements IMutableList<T>, IObservableList<T> {
	
	private final List<T> data;
	private final ListObserversCollection<T> observers;
	
	public MutableObservableList() {
		this.data = new ArrayList<T>();
		this.observers = new ListObserversCollection<T>();
	}

	@Override
	public void addObserver(IListObserver<T> observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(IListObserver<T> observer) {
		observers.remove(observer);
	}

	@Override
	public T getAt(int index) {
		return data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public void insert(int index, T value) {
		data.add(index, value);
		observers.added(index, 1);
	}

	@Override
	public void remove(int index, int count) {
		if (index < 0 || index >= data.size()) {
			throw new IndexOutOfBoundsException();
		}
		
		int length = count;
		
		if (index + length > data.size()) {
			length -= data.size() - index;
		}
		
		if (length > 0) {
			ArrayList<T> removedValues = new ArrayList<T>(length);
			for (int i = 0; i < length; ++i) {
				removedValues.add(data.get(index));
				data.remove(index);
			}
			
			observers.removed(index, removedValues);
		}
	}

	@Override
	public void clear() {
		if (data.size() > 0) {
			Collection<T> oldData = new ArrayList<T>(data);
			data.clear();
			observers.removed(0, oldData);
		}
	}
	
	@Override
	public void move(int startIndex, int newIndex, int count) {
		if (startIndex < 0 || newIndex < 0 || startIndex >= data.size() || newIndex >= data.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (startIndex != newIndex) {
			int length = count;
			
			if (startIndex + length > data.size()) {
				length -= data.size() - startIndex;
			}
			
			if (length > 0) {
				if (newIndex + length >= data.size()) {
					throw new IndexOutOfBoundsException();
				}
				
				if (startIndex < newIndex) {
					// Move the items from the back of the range up.
					for (int i = 1; i <= length; ++i) {
						swap(data, startIndex + length - i, newIndex + length - i);
					}
				} else {
					// Move the items from the front of the range down.
					for (int i = 0; i < length; ++i) {
						swap(data, startIndex + i, newIndex + i);
					}
				}
				observers.moved(startIndex, newIndex, length);
			}
		}
	}
	
	private static <T> void swap(List<T> data, int i1, int i2) {
		
	}
}
