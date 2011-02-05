package org.torquebox.common.pool;

import org.torquebox.common.spi.InstanceFactory;
import org.torquebox.common.spi.Pool;

public class ManagedPool<T> implements Pool<T> {

    private SimplePool<T> pool;
    private PoolManager<T> poolManager;

    public ManagedPool(InstanceFactory<T> factory) {
        this( factory, 1, 1 );
    }

    public ManagedPool(InstanceFactory<T> factory, int minInstances, int maxInstances) {
        this.pool = new SimplePool<T>();
        this.poolManager = new PoolManager<T>( this.pool, factory, minInstances, maxInstances );
        this.pool.addListener( this.poolManager );
    }

    public void setMinimumInstances(int minInstances) {
        this.poolManager.setMinimumInstances( minInstances );
    }

    public int getMinimumInstances() {
        return this.poolManager.getMininmumInstances();
    }

    public void setMaximumInstances(int maxInstances) {
        this.poolManager.setMaximumInstances( maxInstances );
    }

    public int getMaximumInstances() {
        return this.poolManager.getMaximumInstances();
    }

    public InstanceFactory<T> getInstanceFactory() {
        return this.poolManager.getInstanceFactory();
    }

    public void start() throws InterruptedException {
        this.poolManager.start();
        this.poolManager.waitForMinimumFill();
    }

    @Override
    public T borrowInstance() throws Exception {
        return this.pool.borrowInstance();
    }

    @Override
    public T borrowInstance(long timeout) throws Exception {
        return this.pool.borrowInstance( timeout );
    }

    @Override
    public void releaseInstance(T instance) {
        this.pool.releaseInstance( instance );
    }

    int size() {
        return this.pool.size();
    }

    int borrowedSize() {
        return this.pool.borrowedSize();
    }

    int availableSize() {
        return this.pool.availableSize();
    }

    void waitForInitialFill() throws InterruptedException {
        this.poolManager.waitForMinimumFill();
    }

}
