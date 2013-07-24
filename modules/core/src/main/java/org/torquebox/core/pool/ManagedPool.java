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

import java.util.Set;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.logging.Logger;

public class ManagedPool<T> implements Pool<T> {

    public ManagedPool() {
        this( null, 1, 1);
    }

    public ManagedPool(InstanceFactory<T> factory) {
        this( factory, 1, 1 );
    }

    public ManagedPool(InstanceFactory<T> factory, int minInstances, int maxInstances) {
        this.pool = new SimplePool<T>();
        this.poolManager = new PoolManager<T>( this.pool, factory, minInstances, maxInstances );
        this.pool.addListener( this.poolManager );
    }

    public void setName(String name) {
        this.pool.setName( name );
    }

    public String getName() {
        return this.pool.getName();
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

    public void setInstanceFactory(InstanceFactory<T> instanceFactory) {
        this.poolManager.setInstanceFactory( instanceFactory );
    }

    public InstanceFactory<T> getInstanceFactory() {
        return this.poolManager.getInstanceFactory();
    }

    public synchronized void startPool() throws InterruptedException {
        startPool( false );
    }

    public synchronized void startPool(boolean waitForFill) throws InterruptedException {
        if (!this.started) {
            this.poolManager.start();
            this.started = true;
            if (waitForFill) {
                this.poolManager.waitForMinimumFill();
            }
        }
    }

    public synchronized void start() throws InterruptedException {
        if (this.deferUntilRequested) {
            log.info( "Deferring start for " + getName() + " runtime pool." );
        } else {
            startPool( true );
        }
    }

    public synchronized void stop() throws Exception {
        if (this.started) {
            this.poolManager.waitForAllReturned();
            this.poolManager.stop();
            this.started = false;
            this.poolManager.waitForEmpty();
            log.info( "Stopped runtime pool " + getName() );
        }
    }

    @Override
    public T borrowInstance(String requester) throws Exception {
        if (!this.started) {
            startPool();
        }
        return this.pool.borrowInstance( requester );
    }

    @Override
    public T borrowInstance(String requester, long timeout) throws Exception {
        if (!this.started) {
            startPool();
        }
        return this.pool.borrowInstance( requester, timeout );
    }


    @Override
    public void releaseInstance(T instance) {
        this.pool.releaseInstance( instance );
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isLazy() {
        return isDeferredUntilRequested();
    }

    public boolean isDeferredUntilRequested() {
        return this.deferUntilRequested;
    }

    public void setDeferUntilRequested(boolean deferUntilRequested) {
        this.deferUntilRequested = deferUntilRequested;
    }
    
    public int getSize() {
        return size();
    }

    public int getBorrowed() {
        return borrowedSize();
    }

    public int getAvailable() {
        return availableSize();
    }

    public boolean isDrained() {
        return borrowedSize() == 0;
    }

    protected Set<T> getAllInstances() {
        return this.pool.getAllInstances();
    }

    protected Set<T> getBorrowedInstances() {
        return this.pool.getBorrowedInstances();
    }

    protected Set<T> getAvailableInstances() {
        return this.pool.getAvailableInstances();
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
    
    public void setNamespaceContextSelector(NamespaceContextSelector nsContextSelector) {
        this.nsContextSelector = nsContextSelector;
    }
    
    public NamespaceContextSelector getNamespaceContextSelector() {
        return this.nsContextSelector;
    }

    
    private Logger log = Logger.getLogger( this.getClass() );

    private SimplePool<T> pool;
    private PoolManager<T> poolManager;
    private boolean deferUntilRequested = true;
    private boolean started = false;
    private NamespaceContextSelector nsContextSelector;
    
}
