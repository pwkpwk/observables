package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DispatchingObservableList<T> extends LinkedReadOnlyObservableList<T> {

	private final IDispatcher dispatcher;
	private final ArrayListEx<T> data;
		
	public DispatchingObservableList(IReadOnlyObservableList<T> source, IDispatcher dispatcher) {
		super(source);
		int size = source.getSize();

		this.dispatcher = dispatcher;
		this.data = new ArrayListEx<T>(source.getSize());
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
		
		for (int i = startIndex; i < startIndex + count; ++i) {
			addedItems.add(source.getAt(i));
		}
		
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				data.addAll(startIndex, addedItems);
				notifyAdded(startIndex, count);
			}
		});
	}
	
	@Override
	protected void onRemoving(IReadOnlyObservableList<T> source, int startIndex, int count) {
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				notifyRemoving(startIndex, count);
				data.remove(startIndex, count);
				notifyRemoved(startIndex, count);
			}
		});
	}

	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, int count) {
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				data.move(oldStartIndex, newStartIndex, count);
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
