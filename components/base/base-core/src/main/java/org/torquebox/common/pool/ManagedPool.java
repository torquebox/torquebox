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
    
    public int getSize() {
        return size();
    }
    
    public int getBorrowed() {
        return borrowedSize();
    }
    
    public int getAvailable() {
        return availableSize();
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
