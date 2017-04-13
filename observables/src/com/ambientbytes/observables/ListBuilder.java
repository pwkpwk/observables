package com.ambientbytes.observables;

/**
 * Builder of all read-only observable lists.
 * The builder is seeded with the ultimate source creates a chain of collections that observe each other
 * and add transformations that filter, order or map the source collection.
 * <p><strong>Example</strong></p>
 * <p>The following code takes a source list <i>(sourceList)</i> and creates a chain of observable lists
 * that first applies a filter, then orders elements, then maps them to some other type using a mapper <i>(mapper)</i>,
 * and finally dispatches all changes with a dispatcher to some other thread.</p>
 * <pre>{@code
 * ReadWriteMonitor monitor = LockTool.createReadWriteMonitor(new ReentrantReadWriteLock());
 * MutableObservableReference<IItemFilter<Item>> filter = new MutableObservableReference<>(itemFilter, monitor);
 * MutableObservableRererence<IItemsOrder<Item>> order = new MutableObservableRererence<>(itemsOrder, monitor);
 * Trigger trigger = new Trigger(monitor);
 * 
 * IReadOnlyObservableList<Item> list = ListBuilder.unlinker(trigger)
 *     .source(sourceList, monitor)
 *     .filter(filter)
 *     .order(order)
 *     .map(mapper)
 *     .dispatch(dispatcher)
 *     .build();
 *     
 * // Unlink the chain of lists from the source.
 * // After triggering the trigger the list created by ListBuilder won't be retained by sourceList.
 * // Unlinking eliminated memory leaks
 * trigger.trigger();
 * }</pre>
 * <p></p>
 * @author Pavel Karpenko
 */

public final class ListBuilder<T> {
	
	private ITrigger unlinker;
	
	private ListBuilder(ITrigger unlinker) {
		this.unlinker = unlinker;
	}
	
	//
	// Unlinker that may be attached to observable lists created by list builder child classes if the caller
	// of one of the methods of ListBuilder has provided an unlinking trigger.
	//
    private static final class Unlinker<T> implements ITriggerListener {
    	
    	private final ILinkedReadOnlyObservableList<T> source;
    	private final ITrigger trigger;

    	static <T> IReadOnlyObservableList<T> attachUnlinker(ILinkedReadOnlyObservableList<T> source, ITrigger trigger) {
    		Unlinker<T> unlinker = new Unlinker<>(source, trigger);
    		//
    		// reguster() retains the unlinker object in the trigger.
    		// All unlinkers will remain retained as long as the trigger is retained by the caller.
    		// If the caller will allow the VM to collect the trigger, the VM will also collect all unlinkers,
    		// which is benign because nothing else refers to them.
    		//
    		unlinker.register();
    		return source;
    	}
    	
    	private Unlinker(ILinkedReadOnlyObservableList<T> source, ITrigger trigger) {
    		this.source = source;
    		this.trigger = trigger;
    	}
    	
    	private void register() {
    		trigger.addListener(this);
    	}

		@Override
		public void triggered(ITrigger sender) {
			this.source.unlink();
			//
			// After removing self from the trigger, the unlinker won't have any more references
			// and will be safely collected by the VM.
			//
			this.trigger.removeListener(this);
		}
    	
    }

    private abstract static class MonitoredListBuilder<T> implements IListBuilder<T> {
    	
        private final IReadWriteMonitor monitor;
        private final ITrigger unlinker;

        MonitoredListBuilder(ITrigger unlinker, IReadWriteMonitor monitor) {
            this.monitor = monitor;
            this.unlinker = unlinker;
        }
        
        protected abstract IReadOnlyObservableList<T> buildList();

        protected final IReadWriteMonitor monitor() {
            return monitor;
        }

        @Override
        public final IListBuilder<T> dispatch(IDispatcher dispatcher) {
            return new DispatchingListBuilder<>(this, unlinker, monitor, dispatcher);
        }

        @Override
        public final IListBuilder<T> filter(IObservableReference<IItemFilter<T>> filter) {
            return new FilteringListBuilder<>(this, unlinker, monitor, filter);
        }

        @Override
        public final IListBuilder<T> order(IObservableReference<IItemsOrder<T>> order) {
            return new OrderingListBuilder<>(this, unlinker, monitor, order);
        }

        @Override
        public final <TMapped> IListBuilder<TMapped> map(IItemMapper<T, TMapped> mapper) {
            return new MappingListBuilder<>(this, unlinker, monitor, mapper);
        }
        
        @Override
        public final IReadOnlyObservableList<T> build() {
        	return attachUnlinker(buildList());
        }
        
        protected final IReadOnlyObservableList<T> attachUnlinker(IReadOnlyObservableList<T> list) {
        	if (unlinker != null && list instanceof ILinkedReadOnlyObservableList) {
        		list = Unlinker.attachUnlinker((ILinkedReadOnlyObservableList<T>) list, unlinker);
        	}
        	
        	return list;
        }
    }

    private abstract static class ChainedListBuilder<T> extends MonitoredListBuilder<T> {

        private final IListBuilder<T> source;

        public ChainedListBuilder(IListBuilder<T> source, ITrigger unlinker, IReadWriteMonitor monitor) {
            super(unlinker, monitor);
            this.source = source;
        }

        protected final IReadOnlyObservableList<T> buildSource() {
            return source.build();
        }
    }

    private final static class StraightListBuilder<T> extends MonitoredListBuilder<T> {

        private final IReadOnlyObservableList<T> sourceList;

        StraightListBuilder(IReadOnlyObservableList<T> sourceList, ITrigger unlinker, IReadWriteMonitor monitor) {
            super(unlinker, monitor);
            this.sourceList = sourceList;
        }

        @Override
        public IReadOnlyObservableList<T> buildList() {
            return sourceList;
        }
    }
    
    private final static class MergingListBuilder<T> extends MonitoredListBuilder<T> {
    	private final IListSet<T> listSet;
    	
    	MergingListBuilder(IListSet<T> listSet, ITrigger unlinker, IReadWriteMonitor monitor) {
    		super(unlinker, monitor);
    		this.listSet = listSet;
    	}

		@Override
		public IReadOnlyObservableList<T> buildList() {
			return new MergingReadOnlyObservableList<>(listSet, monitor());
		}
    }

    private final static class DispatchingListBuilder<T> extends ChainedListBuilder<T> {

        private final IDispatcher dispatcher;

        DispatchingListBuilder(IListBuilder<T> source, ITrigger unlinker, IReadWriteMonitor monitor, IDispatcher dispatcher) {
            super(source, unlinker, monitor);
            this.dispatcher = dispatcher;
        }

        @Override
        public IReadOnlyObservableList<T> buildList() {
            return new DispatchingObservableList<>(buildSource(), dispatcher, monitor());
        }
    }

    private final static class FilteringListBuilder<T> extends ChainedListBuilder<T> {

        private final IObservableReference<IItemFilter<T>> filter;

        FilteringListBuilder(IListBuilder<T> source, ITrigger unlinker, IReadWriteMonitor monitor, IObservableReference<IItemFilter<T>> filter) {
            super(source, unlinker, monitor);
            this.filter = filter;
        }

        @Override
        public IReadOnlyObservableList<T> buildList() {
            return new FilteringReadOnlyObservableList<>(buildSource(), filter, monitor());
        }
    }

    private final static class OrderingListBuilder<T> extends ChainedListBuilder<T> {

        private final IObservableReference<IItemsOrder<T>> order;

        OrderingListBuilder(IListBuilder<T> source, ITrigger unlinker, IReadWriteMonitor monitor, IObservableReference<IItemsOrder<T>> order) {
            super(source, unlinker, monitor);
            this.order = order;
        }

        @Override
        public IReadOnlyObservableList<T> buildList() {
            return new OrderingReadOnlyObservableList<>(buildSource(), order, monitor());
        }
    }

    private final static class MappingListBuilder<TSource, TMapped> extends MonitoredListBuilder<TMapped> {

        private final IListBuilder<TSource> source;
        private final IItemMapper<TSource, TMapped> mapper;

        public MappingListBuilder(IListBuilder<TSource> source, ITrigger unlinker, IReadWriteMonitor monitor, IItemMapper<TSource, TMapped> mapper) {
            super(unlinker, monitor);
            this.source = source;
            this.mapper = mapper;
        }

        @Override
        protected final IReadOnlyObservableList<TMapped> buildList() {
            return new MappingReadOnlyObservableList<>(source.build(), mapper, monitor());
        }
    }

    /**
     * Create a new list builder that simply returns the specified observable list.
     * @param source observable list returned by the returned builder.
     * @param monitor read/write monitor propagated to all chained list builders.
     * @return new list builder that returns the specified list.
     */
    public static <T> IListBuilder<T> forSource(IReadOnlyObservableList<T> source, IReadWriteMonitor monitor) {
        return new StraightListBuilder<>(source, null, monitor);
    }
    
    /**
     * Factory of ListBuilder objects with an injected unlinker trigger.
     * @param unlinker unlinker trigger.
     * @return new instance of ListBuilder that will build chains of observable collections with an atached unlinker trigger.
     */
    public static <T> ListBuilder<T> unlinker(ITrigger unlinker) {
    	return new ListBuilder<>(unlinker);
    }

    /**
     * Create the initial IListBuilder object that will build a chain of observable lists.
     * @param source source observable list.
     * @param monitor read/write monitor that will be passed to all observable lists in the chain.
     * @return new builder that will return the source list.
     */
    public IListBuilder<T> source(IReadOnlyObservableList<T> source, IReadWriteMonitor monitor) {
    	return new StraightListBuilder<>(source, unlinker, monitor);
    }

    /**
     * Create a new list builder that creates an observable list that merges contents of lists in the passed list set.
     * @param sources collection of source observable lists.
     * @param monitor read/write monitor propagated to all chained list builders.
     * @return new list builder that creates a new merging observable list.
     */
    public static <T> IListBuilder<T> forMerge(IListSet<T> sources, IReadWriteMonitor monitor) {
    	return new MergingListBuilder<>(sources, null, monitor);
    }

    /**
     * Create a new list builder that creates an observable list that merges contents of lists in the passed list set.
     * @param sources collection of source observable lists.
     * @param monitor read/write monitor propagated to all chained list builders.
     * @return new list builder that creates a new merging observable list.
     */
    public IListBuilder<T> merge(IListSet<T> sources, IReadWriteMonitor monitor) {
    	return new MergingListBuilder<>(sources, unlinker, monitor);
    }
}
