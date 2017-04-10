package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;

final class MappingReadOnlyObservableList<TSource, TMapped> implements ILinkedReadOnlyObservableList<TMapped> {

	private ListObservers<TMapped> observers;
	private final IItemMapper<TSource, TMapped> mapper;
	private final ArrayListEx<TMapped> data;
	private IListObserver sourceObserver;
	private IReadOnlyObservableList<TSource> source;

	MappingReadOnlyObservableList(
			IReadOnlyObservableList<TSource> source,
			IItemMapper<TSource, TMapped> mapper,
            IReadWriteMonitor monitor) {
		this.observers = new ListObservers<>(monitor);
		this.mapper = mapper;
		this.data = new ArrayListEx<>(source.getSize());
		this.source = source;
		for (int i = 0; i < source.getSize(); ++i) {
			this.data.add(mapper.map(source.getAt(i)));
		}
		this.sourceObserver = new IListObserver() {
			@Override public void added(int startIndex, int count)
			{
				onAdded(startIndex, count);
			}
			@Override public void changing(int startIndex, int count) { /* do nothing */ }
			@Override public void changed(int startIndex, int count) { onChanged(startIndex, count); }
			@Override public void removing(int startIndex, int count) { onRemoving(startIndex, count); }
			@Override public void removed(int startIndex, int count) { /* do nothing */ }
			@Override public void moved(int oldStartIndex, int newStartIndex, int count) { onMoved(oldStartIndex, newStartIndex, count); }
			@Override public void resetting() { onResetting(); }
			@Override public void reset() { onReset(); }
		};
		this.source.addObserver(sourceObserver);		
	}

	@Override
	public TMapped getAt(int index) {
		return data.get(index);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public void addObserver(IListObserver observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(IListObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void unlink() {
		if (source != null) {
			source.removeObserver(sourceObserver);
			source = null;
			sourceObserver = null;
		}
	}
	
	private void onAdded(int startIndex, int count) {
		Collection<TMapped> mapped = new ArrayList<>(count);
		
		for (int i = 0; i < count; ++i) {
            TSource sourceValue = source.getAt(startIndex + i);
            TMapped mappedValue = mapper.map(sourceValue);
			mapped.add(mappedValue);
		}
		data.addAll(startIndex, mapped);
		observers.added(startIndex, count);
	}
	
	private void onChanged(int startIndex, int count) {
		observers.changing(startIndex, count);
		for (int i = startIndex; i < startIndex + count; ++i) {
			data.set(i, mapper.map(source.getAt(i)));
		}
		observers.changed(startIndex, count);
	}
	
	private void onRemoving(int startIndex, int count) {
		observers.removing(startIndex, count);
		data.remove(startIndex, count);
		observers.removed(startIndex, count);
	}
	
	private void onMoved(int oldStartIndex, int newStartIndex, int count) {
		final int low, pivot, high;
		
		if (oldStartIndex < newStartIndex) {
			low = oldStartIndex;
			pivot = oldStartIndex + count;
			high = newStartIndex + count;
		} else {
			low = newStartIndex;
			pivot = oldStartIndex;
			high = pivot + count;
		}
				
		reverseRange(low, pivot);
		reverseRange(pivot, high);
		reverseRange(low, high);

		observers.moved(oldStartIndex, newStartIndex, count);
	}
	
	private void onResetting() {
		observers.resetting();
	}
	
	private void onReset() {
		final int size = source.getSize();
		
		data.clear();
		for (int i = 0; i < size; ++i) {
			data.add(mapper.map(source.getAt(i)));
		}
		observers.reset();
	}
	
	private void reverseRange(int low, int high) {
		if (high - low > 1) {
			int l = low;
			int h = high - 1;
			
			while (l < h) {
				TMapped tmp = data.get(l);
				data.set(l, data.get(h));
				data.set(h,  tmp);
				l++;
				h--;
			}
		}
	}

}
