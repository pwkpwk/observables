package com.ambientbytes.observables;

/**
 * Builder of all read-only observable lists.
 * The builder is seeded with the ultimate source creates a chain of collections that observe each other
 * and add transformations that filter, order or map the source collection.
 * @author Pavel Karpenko
 */

public final class ListBuilder<T> {

    private abstract static class MonitoredListBuilder<T> implements IListBuilder<T> {
        private final IReadWriteMonitor monitor;

        MonitoredListBuilder(IReadWriteMonitor monitor) {
            this.monitor = monitor;
        }

        protected final IReadWriteMonitor monitor() {
            return monitor;
        }

        @Override
        public final IListBuilder<T> dispatch(IDispatcher dispatcher) {
            return new DispatchingListBuilder<>(this, monitor, dispatcher);
        }

        @Override
        public final IListBuilder<T> filter(IObservableReference<IItemFilter<T>> filter) {
            return new FilteringListBuilder<>(this, monitor, filter);
        }

        @Override
        public final IListBuilder<T> order(IObservableReference<IItemsOrder<T>> order) {
            return new OrderingListBuilder<>(this, monitor, order);
        }

        @Override
        public <TMapped> IListBuilder<TMapped> map(IItemMapper<T, TMapped> mapper) {
            return new MappingListBuilder<>(this, monitor, mapper);
        }
    }

    private abstract static class ChainedListBuilder<T> extends MonitoredListBuilder<T> {

        private final IListBuilder<T> source;

        public ChainedListBuilder(IListBuilder<T> source, IReadWriteMonitor monitor) {
            super(monitor);
            this.source = source;
        }

        protected final IReadOnlyObservableList<T> buildSource() {
            return source.build();
        }
    }

    private final static class StraightListBuilder<T> extends MonitoredListBuilder<T> {

        private final IReadOnlyObservableList<T> sourceList;

        StraightListBuilder(IReadOnlyObservableList<T> sourceList, IReadWriteMonitor monitor) {
            super(monitor);
            this.sourceList = sourceList;
        }

        @Override
        public IReadOnlyObservableList<T> build() {
            return sourceList;
        }
    }
    
    private final static class MergingListBuilder<T> extends MonitoredListBuilder<T> {
    	private final IListSet<T> listSet;
    	
    	MergingListBuilder(IListSet<T> listSet, IReadWriteMonitor monitor) {
    		super(monitor);
    		this.listSet = listSet;
    	}

		@Override
		public IReadOnlyObservableList<T> build() {
			return new MergingReadOnlyObservableList<>(listSet, monitor());
		}
    }

    private final static class DispatchingListBuilder<T> extends ChainedListBuilder<T> {

        private final IDispatcher dispatcher;

        DispatchingListBuilder(IListBuilder<T> source, IReadWriteMonitor monitor, IDispatcher dispatcher) {
            super(source, monitor);
            this.dispatcher = dispatcher;
        }

        @Override
        public IReadOnlyObservableList<T> build() {
            return new DispatchingObservableList<>(buildSource(), dispatcher, monitor());
        }
    }

    private final static class FilteringListBuilder<T> extends ChainedListBuilder<T> {

        private final IObservableReference<IItemFilter<T>> filter;

        FilteringListBuilder(IListBuilder<T> source, IReadWriteMonitor monitor, IObservableReference<IItemFilter<T>> filter) {
            super(source, monitor);
            this.filter = filter;
        }

        @Override
        public IReadOnlyObservableList<T> build() {
            return new FilteringReadOnlyObservableList<>(buildSource(), filter, monitor());
        }
    }

    private final static class OrderingListBuilder<T> extends ChainedListBuilder<T> {

        private final IObservableReference<IItemsOrder<T>> order;

        OrderingListBuilder(IListBuilder<T> source, IReadWriteMonitor monitor, IObservableReference<IItemsOrder<T>> order) {
            super(source, monitor);
            this.order = order;
        }

        @Override
        public IReadOnlyObservableList<T> build() {
            return new OrderingReadOnlyObservableList<>(buildSource(), order, monitor());
        }
    }

    private final static class MappingListBuilder<TSource, TMapped> extends MonitoredListBuilder<TMapped> {

        private final IListBuilder<TSource> source;
        private final IItemMapper<TSource, TMapped> mapper;

        public MappingListBuilder(IListBuilder<TSource> source, IReadWriteMonitor monitor, IItemMapper<TSource, TMapped> mapper) {
            super(monitor);
            this.source = source;
            this.mapper = mapper;
        }

        @Override
        public IReadOnlyObservableList<TMapped> build() {
            return new MappingReadOnlyObservableList<>(source.build(), mapper, monitor());
        }
    }

    /**
     * Create a new list builder that simply returns the specified observable list.
     * @param source observable list returned by the returned builder.
     * @param monitor read/write monitor propagated to all chained list builders.
     * @return new list builder that returns the specified list.
     */
    public static <T> IListBuilder<T> source(IReadOnlyObservableList<T> source, IReadWriteMonitor monitor) {
        return new StraightListBuilder<>(source, monitor);
    }

    /**
     * Create a new list builder that creates an observable list that merges contents of lists in the passed list set.
     * @param sources collection of source observable lists.
     * @param monitor read/write monitor propagated to all chained list builders.
     * @return new list builder that creates a new merging observable list.
     */
    public static <T> IListBuilder<T> merge(IListSet<T> sources, IReadWriteMonitor monitor) {
    	return new MergingListBuilder<>(sources, monitor);
    }
}
