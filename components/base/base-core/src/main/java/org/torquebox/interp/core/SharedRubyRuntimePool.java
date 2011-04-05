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

package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.torquebox.common.pool.SharedPool;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RubyRuntimePool;

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
    public Ruby borrowRuntime() throws Exception {
        return borrowInstance();
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
            ruby = borrowRuntime();
            return ruby.evalScriptlet( code );
        } finally {
            if ( ruby != null ) {
                returnRuntime( ruby );
            }
        }
    }

}
