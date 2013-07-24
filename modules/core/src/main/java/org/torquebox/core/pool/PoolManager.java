/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.logging.Logger;

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
                log.error(  "Error managing pool", e );
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

    private SimplePool<T> pool;
    private InstanceFactory<T> factory;

    private int minInstances;
    private int maxInstances;

    private Semaphore instances;

    private ExecutorService executor;
    private FillTask<T> fillTask;

    private boolean started = false;

    private NamespaceContextSelector nsContextSelector;

    public PoolManager(SimplePool<T> pool, InstanceFactory<T> factory, int minInstances, int maxInstances) {
        this.pool = pool;
        this.factory = factory;
        this.minInstances = minInstances;
        this.maxInstances = maxInstances;

        this.fillTask = new FillTask<T>( this );
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

    @Override
    public void instanceRequested(int totalInstances, int availableNow) {
        log.debug( "instanceRequested - totalInstances = " + totalInstances +
                ", availableNow = " + availableNow + ", availablePermits = " +
                this.instances.availablePermits() );

        if (this.instances.tryAcquire()) {
            this.executor.execute( this.fillTask );
        }
    }

    protected void fillInstance() throws Exception {
        if (this.started) { // don't fill an instance if we've stopped
            if (this.nsContextSelector != null) {
                NamespaceContextSelector.pushCurrentSelector( this.nsContextSelector );
            }
            try {
                T instance = this.factory.createInstance( this.pool.getName() );
                this.pool.fillInstance( instance );
            } finally {
                if (this.nsContextSelector != null) {
                    NamespaceContextSelector.popCurrentSelector();
                }
            }
        }
    }

    protected void drainInstance() throws Exception {
        synchronized (this.pool) {
            T instance = this.pool.drainInstance();
            this.factory.destroyInstance( instance );
        }
    }

    public synchronized void start() {
        started = true;
        this.instances = new Semaphore( this.maxInstances - this.minInstances, true );
        if (this.executor == null) {
            int threadSize = Math.min( Math.max( this.minInstances, 1 ),
                    Runtime.getRuntime().availableProcessors() * 3 );
            this.executor = Executors.newFixedThreadPool( threadSize );
        }
        for (int i = 0; i < this.minInstances; ++i) {
            this.executor.execute( this.fillTask );
        }

    }

    public synchronized void stop() throws Exception {
        started = false;
        while (pool.size() > 0) {
            drainInstance();
        }
        this.executor.shutdown();
    }

    public void waitForMinimumFill() throws InterruptedException {
        while (this.pool.size() < this.minInstances) {
            Thread.sleep( 50 );
        }
    }

    public void waitForEmpty() throws InterruptedException {
        while (this.pool.size() > 0) {
            Thread.sleep( 50 );
        }
    }

    public void waitForAllReturned() throws InterruptedException {
        while (this.pool.borrowedSize() > 0) {
            Thread.sleep( 50 );
        }
    }

    public void setNamespaceContextSelector(NamespaceContextSelector nsContextSelector) {
        this.nsContextSelector = nsContextSelector;
    }

    public NamespaceContextSelector getNamespaceContextSelector() {
        return this.nsContextSelector;
    }

    private static final Logger log = Logger.getLogger( PoolManager.class );

}
