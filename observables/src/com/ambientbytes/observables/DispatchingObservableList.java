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
			addedItems.add(source.getAt(i));
		}
		
		dispatcher.dispatch(new IAction() {
			@Override
			public void execute() {
				for (int i = 0; i < count; ++i) {
					data.add(startIndex + 1, addedItems.get(i));
				}
				notifyAdded(startIndex, count);
			}
		});
	}

	@Override
	protected void onRemoved(IReadOnlyObservableList<T> source, int startIndex, Collection<T> items) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMoved(IReadOnlyObservableList<T> source, int oldStartIndex, int newStartIndex, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onReset(IReadOnlyObservableList<T> source, Collection<T> items) {
		// TODO Auto-generated method stub
		
	}
}
