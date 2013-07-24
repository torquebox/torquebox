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

import java.util.Collections;
import java.util.Set;

import org.jruby.Ruby;
import org.torquebox.core.pool.SharedPool;
import org.torquebox.core.util.RuntimeHelper;

/**
 * Ruby interpreter pool which shares a single {@link Ruby} instance.
 * 
 * <p>
 * If constructed with an instance, the instance will be given out to all
 * consumers of the pool, without bounds.
 * </p>
 * 
 * <p>
 * If constructed with an instance factory, the factory will be used to create
 * exactly one instance, which is then shared with all consumers, without
 * bounds.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 */
public class SharedRubyRuntimePool extends SharedPool<Ruby> implements RubyRuntimePool, BasicRubyRuntimePoolMBean {

    public SharedRubyRuntimePool() {
        
    }
    
    /**
     * Construct with a factory.
     * 
     * @param factory
     *            The factory to create the shared instance.
     */
    public SharedRubyRuntimePool(RubyRuntimeFactory factory) {
        super( factory );
    }

    /**
     * Construct with an instance.
     * 
     * @param ruby
     *            The shared instance.
     */
    public SharedRubyRuntimePool(Ruby ruby) {
        super( ruby );
    }

    @Override
    public Ruby borrowRuntime(String requester) throws Exception {
        return borrowInstance( requester );
    }

    @Override
    public void returnRuntime(Ruby runtime) {
        releaseInstance( runtime );
    }

    /**
     * Retrieve the runtime instance factory used.
     * 
     * @return The instance factory, or {@code null} if an instance was provided
     *         directly.
     */
    public RubyRuntimeFactory getRubyRuntimeFactory() {
        return (RubyRuntimeFactory) getInstanceFactory();
    }

    /**
     * Retrieve the shared runtime instance.
     * 
     * @return The shared runtime instance, if initialized, otherwise
     *         {@code null}.
     */
    public Ruby getRuntime() {
        return getInstance();
    }
    
    @Override
    public Object evaluate(String code) throws Exception {
        Ruby ruby = null;
        
        try {
            ruby = borrowRuntime( "anonymous-evaluate" );
            return RuntimeHelper.evalScriptlet( ruby, code );
        } finally {
            if ( ruby != null ) {
                returnRuntime( ruby );
            }
        }
    }

    @Override
    public Set<String> getAllRuntimeNames() {
        return Collections.singleton( "" + getRuntime().hashCode() );
    }

    @Override
    public RubyRuntimePool duplicate() {
        SharedRubyRuntimePool duplicate = new SharedRubyRuntimePool( getRubyRuntimeFactory() );
        duplicate.setName( getName() );
        duplicate.setDeferUntilRequested( isDeferredUntilRequested() );
        duplicate.setNamespaceContextSelector( getNamespaceContextSelector() );
        return duplicate;
    }

    @Override
    public void setMinimumInstances(int minInstances) {
        // no-op, always 1
    }

    @Override
    public int getMinimumInstances() {
        return 1;
    }

    @Override
    public void setMaximumInstances(int maxInstances) {
        // no-op, always 1
    }

    @Override
    public int getMaximumInstances() {
        return 1;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getBorrowed() {
        return 0;
    }

    @Override
    public int getAvailable() {
        return 1;
    }

}
