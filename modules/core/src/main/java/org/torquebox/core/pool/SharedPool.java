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

package org.torquebox.core.pool;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.logging.Logger;

/**
 * A pool implementation that shares a single instance to all consumers.
 * 
 * <p>
 * The pool may be primed with either an instance directly or by providing a
 * factory capable of creating the instance. In the case that a
 * {@link InstanceFactory} is used, exactly one instance will be created.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @param <T>
 *            The poolable resource.
 */
public class SharedPool<T> implements Pool<T> {

    protected Logger log = Logger.getLogger( getClass() );

    /**
     * Construct.
     */
    public SharedPool() {
    }

    /**
     * Construct with an instance factory.
     * 
     * @param factory
     *            The factory to create the initial instance.
     */
    public SharedPool(InstanceFactory<T> factory) {
        this.factory = factory;
    }

    /**
     * Construct with an instance.
     * 
     * @param instance
     *            The initial instance.
     */
    public SharedPool(T instance) {
        this.instance = instance;
    }

    /**
     * Set the pool name.
     * 
     * @param name
     *            The pool name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieve the pool name.
     * 
     * @return The pool name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the shared instance.
     * 
     * @param instance
     *            The initial instance.
     */
    public void setInstance(T instance) {
        this.instance = instance;
    }

    /**
     * Retrieve the shared instance.
     * 
     * @return The shared instance.
     */
    public T getInstance() {
        return this.instance;
    }

    /**
     * Set the instance factory to create the initial instance.
     * 
     * @param factory
     *            The instance factory.
     */
    public void setInstanceFactory(InstanceFactory<T> factory) {
        this.factory = factory;
    }

    /**
     * Retrieve the instance factory to create the initial instance.
     * 
     * @return The instance factory;
     */
    public InstanceFactory<T> getInstanceFactory() {
        return this.factory;
    }

    public synchronized void startPool() throws Exception {
        if (this.instance == null) {
            if (this.nsContextSelector != null) {
                NamespaceContextSelector.pushCurrentSelector( this.nsContextSelector );
            }
            try {
                this.instance = factory.createInstance( getName() );
            } finally {
                if (this.nsContextSelector != null) {
                    NamespaceContextSelector.popCurrentSelector();
                }
            }
        }
    }

    public boolean isDeferredUntilRequested() {
        return this.deferUntilRequested;
    }

    public void setDeferUntilRequested(boolean deferUntilRequested) {
        this.deferUntilRequested = deferUntilRequested;
    }

    public boolean isStartAsynchronously() {
        return startAsynchronously;
    }

    public void setStartAsynchronously(boolean startAsynchronously) {
        this.startAsynchronously = startAsynchronously;
    }

    /**
     * Create the pool.
     * 
     * @throws Exception
     *             if an error occurs starting the pool.
     */
    public synchronized void start() throws Exception {
        if (this.instance != null) {
            return;
        }

        if (this.factory == null) {
            throw new IllegalArgumentException( "Neither an instance nor an instance-factory provided." );
        }
        if (!this.deferUntilRequested && !this.startAsynchronously) {
            startPool();
        } else if (this.startAsynchronously) {
            log.info( "Starting " + this.name + " runtime pool asynchronously" );
            Thread initThread = new Thread() {
                public void run() {
                    try {
                        SharedPool.this.startPool();
                    } catch (Exception ex) {
                        log.error( "Failed to start pool", ex );
                    }
                }
            };
            initThread.start();
        } else {
            log.info( "Deferring start for " + this.name + " runtime pool." );
        }
    }

    /**
     * Destroy the pool.
     */
    public synchronized void stop() {
        if (this.factory != null && this.instance != null) {
            this.factory.destroyInstance( this.instance );
        }
        this.instance = null;
        this.factory = null;
    }

    @Override
    public T borrowInstance(String requester) throws Exception {
        return borrowInstance( requester, 0 );
    }

    @Override
    public void releaseInstance(T instance) {
        // nothing
    }

    @Override
    public synchronized T borrowInstance(String requester, long timeout) throws Exception {
        if (this.instance == null) {
            startPool();
        }

        long remaining = timeout;
        while (this.instance == null) {
            long startWait = System.currentTimeMillis();
            this.wait( timeout );
            remaining = remaining - (System.currentTimeMillis() - startWait);
            if (remaining <= 0) {
                break;
            }
        }

        return this.instance;
    }
    
    public void setNamespaceContextSelector(NamespaceContextSelector nsContextSelector) {
        this.nsContextSelector = nsContextSelector;
    }
    
    public NamespaceContextSelector getNamespaceContextSelector() {
        return this.nsContextSelector;
    }

    /** Name of the pool. */
    private String name = "anonymous-pool";

    /** The shared instance. */
    private T instance;

    /** Optional factory to create the initial instance. */
    private InstanceFactory<T> factory;

    private boolean deferUntilRequested = true;

    private boolean startAsynchronously = false;

    private NamespaceContextSelector nsContextSelector = null;

}
