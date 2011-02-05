package org.torquebox.common.pool;

import org.torquebox.common.spi.PoolListener;

public class DefaultPoolListener<T> implements PoolListener<T> {

    @Override
    public void instanceBorrowed(T instance, int totalInstances, int availableNow) {
    }

    @Override
    public void instanceDrained(T instance, int totalInstances, int availableNow) {
    }

    @Override
    public void instanceFilled(T instance, int totalInstances, int availableNow) {
    }

    @Override
    public void instanceReleased(T instance, int totalInstances, int availableNow) {
    }

    @Override
    public void instanceRequested(int totalInstances, int availableNow) {
    }

}
