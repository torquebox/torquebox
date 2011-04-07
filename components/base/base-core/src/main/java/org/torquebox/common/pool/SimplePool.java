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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.torquebox.common.spi.ManageablePool;
import org.torquebox.common.spi.PoolListener;

public class SimplePool<T> implements ManageablePool<T> {

    private String name;
    private final Set<T> instances = new HashSet<T>();
    private final Set<T> borrowedInstances = new HashSet<T>();
    private final Set<T> availableInstances = new HashSet<T>();

    private final PoolListeners<T> listeners = new PoolListeners<T>();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public void addListener(PoolListener<T> listener) {
        this.listeners.getListeners().add( listener );
    }

    public boolean removeListener(PoolListener<T> listener) {
        return this.listeners.getListeners().remove( listener );
    }

    @Override
    public T borrowInstance() throws Exception {
        return borrowInstance( 0 );
    }

    public T borrowInstance(long timeout) throws InterruptedException {
        this.listeners.instanceRequested( instances.size(), availableInstances.size() );
        long start = System.currentTimeMillis();
        synchronized (this.instances) {
            while (availableInstances.isEmpty()) {
                long remainingTime = ((timeout == 0) ? 0 : (timeout - (System.currentTimeMillis() - start)));
                if ((timeout != 0) && (remainingTime <= 0)) {
                    return null;
                }
                this.instances.wait( remainingTime );
            }

            Iterator<T> iter = this.availableInstances.iterator();
            T instance = iter.next();
            iter.remove();

            this.borrowedInstances.add( instance );

            this.listeners.instanceBorrowed( instance, instances.size(), availableInstances.size() );
            return instance;
        }
    }

    @Override
    public void releaseInstance(T instance) {
        synchronized (this.instances) {
            this.borrowedInstances.remove( instance );
            this.availableInstances.add( instance );
            this.listeners.instanceReleased( instance, instances.size(), availableInstances.size() );
            this.instances.notifyAll();
        }

    }

    public void fillInstance(T instance) {
        synchronized (this.instances) {
            this.instances.add( instance );
            this.availableInstances.add( instance );
            this.instances.notifyAll();
        }
    }

    public T drainInstance() throws Exception {
        return drainInstance( 0 );
    }

    public T drainInstance(long timeout) throws Exception {
        synchronized (this.instances) {
            T instance = borrowInstance( timeout );
            this.borrowedInstances.remove( instance );
            this.instances.remove( instance );
            return instance;
        }
    }
    
    Set<T> getAllInstances() {
        Set<T> instances = new HashSet<T>();
        instances.addAll( this.instances );
        return instances;
    }
    
    Set<T> getBorrowedInstances() {
        Set<T> instances = new HashSet<T>();
        instances.addAll( this.borrowedInstances);
        return instances;
    }
    
    Set<T> getAvailableInstances() {
        Set<T> instances = new HashSet<T>();
        instances.addAll( this.availableInstances );
        return instances;
    }

    int size() {
        synchronized (instances) {
            return this.instances.size();
        }
    }

    int borrowedSize() {
        synchronized (instances) {
            return this.borrowedInstances.size();
        }
    }

    int availableSize() {
        synchronized (instances) {
            return this.availableInstances.size();
        }
    }

}
