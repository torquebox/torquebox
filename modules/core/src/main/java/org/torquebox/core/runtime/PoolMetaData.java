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

package org.torquebox.core.runtime;

import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

/**
 * Ruby interpreter pool configuration.
 * 
 * <p>
 * Pools may be created as non-shared, shared, or backed by the global
 * interpreter. These three types of pools are ranked from most memory-intensive
 * (but more secure) to less memory-intensive (but less secure, due to sharing).
 * Threadsafety of the client application should be considered when configuring
 * a pool.
 * </p>
 * 
 * <p>
 * A non-shared pool allows exclusive access to each pooled interpreter to
 * exactly one client at a time. A non-shared pool has minimum and maximum size
 * settings.
 * </p>
 * 
 * <p>
 * A shared pool is a pool backed by a single interpreter, shared among many
 * client simultaneously. The pool owns the interpreter.
 * </p>
 * 
 * <p>
 * A global pool is backed by a single global interpreter, possibly shared with
 * other global pools.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 */
public class PoolMetaData {

    /**
     * Pool types.
     */
    public enum PoolType {
        NON_SHARED, SHARED, GLOBAL,
    }

    public static final AttachmentKey<AttachmentList<PoolMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList(PoolMetaData.class);

    /**
     * Named SHARED
     */
    public PoolMetaData(String name) {
        this( name, PoolType.SHARED );
    }

    /**
     * Named NON_SHARED
     */
    public PoolMetaData(String name, int min, int max) {
        this.name = name;
        setMinimumSize( min );
        setMaximumSize( max );
    }

    /**
     * Named specific type
     */
    public PoolMetaData(String name, PoolType type) {
        this.name = name;
        switch (type) {
        case SHARED:
            setShared();
            break;
        case GLOBAL:
            setGlobal();
            break;
        default:
            this.poolType = PoolType.NON_SHARED;
        }
    }

    public static PoolMetaData extractNamedMetaData(List<PoolMetaData> list, String name) {
        for ( PoolMetaData each : list ) {
             if ( each.getName().equals( name ) ) {
                 return each;
             }
         }
        return null;
    }
    
    /**
     * Retrieve the name of the pool.
     * 
     * @return The name of the pool.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the minimum size of the pool.
     * 
     * @param minimumSize
     *            The minimum size of the pool.
     */
    public void setMinimumSize(int minimumSize) {
        this.minimumSize = minimumSize;
        this.poolType = PoolType.NON_SHARED;
    }

    /**
     * Retrieve the minimum size of the pool.
     * 
     * @return The minimum size of the pool.
     */
    public int getMinimumSize() {
        return this.minimumSize;
    }

    /**
     * Set the maximum size of the pool.
     * 
     * @param maximumSize
     *            The maximum size of the pool.
     */
    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        this.poolType = PoolType.NON_SHARED;
    }

    /**
     * Retrieve the maximum size of the pool.
     * 
     * @return The maximum size of the pool.
     */
    public int getMaximumSize() {
        return this.maximumSize;
    }

    /**
     * Configure this pool to share a single instance.
     */
    public void setShared() {
        this.poolType = PoolType.SHARED;
        this.minimumSize = -1;
        this.maximumSize = -1;
    }

    /**
     * Determine if this pool is configured for shared access to a single
     * instance.
     * 
     * @return {@code true} if so, otherwise {@code false}.
     */
    public boolean isShared() {
        return (this.poolType == PoolType.SHARED);
    }

    /**
     * Set the optional instance-factory name.
     * 
     * @param instanceFactoryName
     *            The name of the instance factory to use if non-global.
     */
    public void setInstanceFactoryName(String instanceFactoryName) {
        this.instanceFactoryName = instanceFactoryName;
    }

    /**
     * Retrieve the instance-factory name.
     * 
     * @return The name of instance factory to use if non-global.
     */
    public String getInstanceFactoryName() {
        return this.instanceFactoryName;
    }

    /**
     * Set the instance name for shared pools.
     * 
     * @param instanceName
     *            The instance name for shared pools.
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * Retrieve the instance name for shared pools.
     * 
     * @return The instance name for shared pools.
     */
    public String getInstanceName() {
        return this.instanceName;
    }

    /**
     * Configure this pool to share the global instance.
     */
    public void setGlobal() {
        this.poolType = PoolType.GLOBAL;
        this.minimumSize = -1;
        this.maximumSize = -1;
    }

    /**
     * Determine if this pool is configured for shared access to the global
     * instance.
     * 
     * @return {@code true} if so, otherwise {@code false}.
     */
    public boolean isGlobal() {
        return (this.poolType == PoolType.GLOBAL);
    }

    
    public boolean isDeferUntilRequested() {
        return deferUntilRequested;
    }

    public void setDeferUntilRequested(boolean deferUntilRequested) {
        this.deferUntilRequested = deferUntilRequested;
    }

    public String toString() {
        if (this.poolType == PoolType.NON_SHARED) {
            return "[PoolMetaData: name=" + this.name + " min=" + this.minimumSize + " max=" + this.maximumSize + "]";
        } else {
            return "[PoolMetaData: name=" + this.name + " type=" + this.poolType + "]";
        }
    }

    /** Name of the pool. */
    private String name;

    /** Type of pool. */
    private PoolType poolType;

    /** Minimum size of the pool. */
    private int minimumSize;

    /** Maximum size of the pool. */
    private int maximumSize;

    /** Name of the instance-factory to use if non-global. */
    private String instanceFactoryName;

    private String instanceName;
    
    private boolean deferUntilRequested = true;
}
