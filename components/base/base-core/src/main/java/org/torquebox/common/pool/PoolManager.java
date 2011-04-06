/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.common.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.jboss.logging.Logger;
import org.torquebox.common.spi.InstanceFactory;

public class PoolManager<T> extends DefaultPoolListener<T> {
    
    private static final Logger log = Logger.getLogger( PoolManager.class );

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
    
    // TODO: Use this!
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
        T instance = this.factory.createInstance( this.pool.getName() );
        this.pool.fillInstance( instance );
    }

    protected void drainInstance() throws Exception {
        T instance = this.pool.drainInstance();
        this.factory.destroyInstance( instance );
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
        int poolSize = pool.size();
        
        for ( int i = 0 ; i < poolSize ; ++i ) {
            this.executor.execute( this.drainTask );
        }

    }

    public void waitForMinimumFill() throws InterruptedException {
        while (this.pool.size() < this.minInstances) {
            Thread.sleep( 50 );
        }
    }
    
    public void waitForEmpty() throws InterruptedException {
        while (this.pool.size() > 0 ) {
            Thread.sleep( 50 );
        }
    }

}
