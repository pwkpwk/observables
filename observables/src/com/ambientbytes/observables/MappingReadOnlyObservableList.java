package com.ambientbytes.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class MappingReadOnlyObservableList<TSource, TMapped> implements ILinkedReadOnlyObservableList<TMapped> {

	private ListObserversCollection<TMapped> observers;
	private final IItemMapper<TSource, TMapped> mapper;
	private final List<TMapped> data;
	private IListObserver<TSource> sourceObserver;
	private IReadOnlyObservableList<TSource> source;

	public MappingReadOnlyObservableList(
			IReadOnlyObservableList<TSource> source,
			IItemMapper<TSource, TMapped> mapper) {
		this.observers = new ListObserversCollection<TMapped>(new DummyReadWriteLock());
		this.mapper = mapper;
		this.data = new ArrayList<>(source.getSize());
		this.source = source;
		for (int i = 0; i < source.getSize(); ++i) {
			this.data.add(mapper.map(source.getAt(i)));
		}
		this.sourceObserver = new IListObserver<TSource>() {
			@Override public void added(int startIndex, int count) { onAdded(startIndex, count); }
			@Override public void removed(int startIndex, Collection<TSource> items) { onRemoved(startIndex, items); }
			@Override public void moved(int oldStartIndex, int newStartIndex, int count) { onMoved(oldStartIndex, newStartIndex, count); }
			@Override public void reset(Collection<TSource> oldItems) { onReset(oldItems); }
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
	public void addObserver(IListObserver<TMapped> observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(IListObserver<TMapped> observer) {
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
	
	private final void onAdded(int startIndex, int count) {
		Collection<TMapped> mapped = new ArrayList<>(count);
		
		for (int i = 0; i < count; ++i) {
			mapped.add(mapper.map(source.getAt(startIndex + i)));
		}
		data.addAll(startIndex, mapped);
		observers.added(startIndex, count);
	}
	
	private final void onRemoved(int startIndex, Collection<TSource> items) {
		final int size = items.size();
		final Collection<TMapped> removed = new ArrayList<>(size);
		
		for (int i = 0; i < size; ++i) {
			TMapped item = data.get(startIndex);
			data.remove(startIndex);
			removed.add(item);
		}
		observers.removed(startIndex, removed);
	}
	
	private final void onMoved(int oldStartIndex, int newStartIndex, int count) {
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
	
	private final void onReset(Collection<TSource> oldItems) {
		Collection<TMapped> removedItems = new ArrayList<>(data);
		final int size = source.getSize();
		
		data.clear();
		for (int i = 0; i < size; ++i) {
			data.add(mapper.map(source.getAt(i)));
		}
		observers.reset(removedItems);
	}
	
	private final void reverseRange(int low, int high) {
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