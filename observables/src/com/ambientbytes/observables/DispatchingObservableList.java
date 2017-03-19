package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DispatchingObservableList<T> extends LinkedReadOnlyObservableList<T> {

	private final IDispatcher dispatcher;
	private final List<T> data;
		
	public DispatchingObservableList(IReadOnlyObservableList<T> source, IDispatcher dispatcher) {
		super(source);
		int size = source.getSize();

		this.dispatcher = dispatcher;
		this.data = new ArrayList<T>(source.getSize());
		for (int i = 0; i < size; ++i) {
			this.data.add(source.getAt(i));
		}
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
	protected void onAdded(IReadOnlyObservableList<T> source, final int startIndex, final int count) {
		final List<T> addedItems = new ArrayList<T>(count);
		
		for (int i = 0; i < count; ++i) {
			addedItems.add(source.getAt(startIndex + i));
		}
		
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				for (int i = 0; i < count; ++i) {
					data.add(startIndex + i, addedItems.get(i));
				}
				notifyAdded(startIndex, count);
			}
		});
	}

	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, Collection<T> items) {
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				for (@SuppressWarnings("unused") T item : items) {
					data.remove(startIndex);
				}
				notifyRemoved(startIndex, items);
			}
		});
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		final List<T> updatedItems;
		final int start, size;
		
		if (oldStartIndex < newStartIndex) {
			start = oldStartIndex;
			size = newStartIndex - oldStartIndex + count;
		} else {
			start = newStartIndex;
			size = oldStartIndex - newStartIndex + count;
		}
		updatedItems = new ArrayList<T>(size);
		
		for (int i = 0; i < size; ++i) {
			updatedItems.add(source.getAt(start + i));
		}
		
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				int index = start;
				for (T item : updatedItems) {
					data.set(index++, item);
				}
				notifyMoved(oldStartIndex, newStartIndex, count);
			}
		});
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source, Collection<T> oldItems) {
		int size = source.getSize();
		final List<T> newItems = new ArrayList<T>(size);
		
		for (int i = 0; i < size; ++i) {
			newItems.add(source.getAt(i));
		}
		
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				data.clear();
				for (T item : newItems) {
					data.add(item);
				}
				notifyReset(oldItems);
			}
		});
	}
}
