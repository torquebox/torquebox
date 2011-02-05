package org.torquebox.common.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.torquebox.common.spi.InstanceFactory;

public class PoolManager<T> extends DefaultPoolListener<T> {

    private static abstract class PoolTask<T> implements Runnable {
        protected PoolManager<T> poolManager;

        public PoolTask(PoolManager<T> poolManager) {
            this.poolManager = poolManager;
        }

        public final void run() {
            try {
                perform();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.poolManager.taskCompleted();
            }
        }

        protected abstract void perform() throws Exception;

    }

    private static class FillTask<T> extends PoolTask<T> {
        public FillTask(PoolManager<T> poolManager) {
            super( poolManager );
        }

        @Override
        public void perform() throws Exception {
            this.poolManager.fillInstance();
        }
    }

    private static class DrainTask<T> extends PoolTask<T> {
        public DrainTask(PoolManager<T> poolManager) {
            super( poolManager );
        }

        @Override
        public void perform() throws Exception {
            this.poolManager.drainInstance();
        }
    }

    private SimplePool<T> pool;
    private InstanceFactory<T> factory;

    private int minInstances;
    private int maxInstances;

    private Semaphore instances;

    private Executor executor;
    private FillTask<T> fillTask;
    private DrainTask<T> drainTask;

    public PoolManager(SimplePool<T> pool, InstanceFactory<T> factory, int minInstances, int maxInstances) {
        this.pool = pool;
        this.factory = factory;
        this.minInstances = minInstances;
        this.maxInstances = maxInstances;
        this.instances = new Semaphore( maxInstances, true );

        this.fillTask = new FillTask<T>( this );
        this.drainTask = new DrainTask<T>( this );
    }

    protected void taskCompleted() {
    }

    public void setMinimumInstances(int minInstances) {
        this.minInstances = minInstances;
    }

    public int getMininmumInstances() {
        return this.minInstances;
    }

    public void setMaximumInstances(int maxInstances) {
        this.maxInstances = maxInstances;
    }

    public int getMaximumInstances() {
        return this.maxInstances;
    }

    public void setInstanceFactory(InstanceFactory<T> factory) {
        this.factory = factory;
    }

    public InstanceFactory<T> getInstanceFactory() {
        return this.factory;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    @Override
    public void instanceRequested(int totalInstances, int availableNow) {
        if (totalInstances >= maxInstances) {
            return;
        }
        if ((availableNow == 0) && (this.instances.tryAcquire())) {
            this.executor.execute( this.fillTask );
        }
    }

    protected void fillInstance() throws Exception {
        T instance = this.factory.create();
        this.pool.fillInstance( instance );
    }

    protected void drainInstance() throws Exception {
        this.pool.drainInstance();
    }

    public void start() {
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor();
        }
        for (int i = 0; i < this.minInstances; ++i) {
            this.executor.execute( this.fillTask );
        }
    }

    public void stop() {

    }

    public void waitForMinimumFill() throws InterruptedException {
        while (this.pool.size() < this.minInstances) {
            Thread.sleep( 50 );
        }

    }

}
