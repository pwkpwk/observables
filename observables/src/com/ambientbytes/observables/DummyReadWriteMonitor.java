package com.ambientbytes.observables;

/**
 * Dummy read/write monitor.
 * @author Pavel Karpenko
 */

final class DummyReadWriteMonitor implements IReadWriteMonitor {

    private static final IResource dummyResource = new IResource() {
        @Override
        public void release() {
            // Do nothing.
        }
    };

    @Override
    public IResource acquireRead() {
        return dummyResource;
    }

    @Override
    public IResource acquireWrite() {
        return dummyResource;
    }
}
