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
import org.torquebox.common.pool.ManagedPool;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RubyRuntimePool;

/**
 * Ruby interpreter pool of discrete, non-shared interpreters.
 * 
 * <p>
 * This pool supports minimum and maximum sizes, and ensures each client gets a
 * unique interpreter.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 */
public class DefaultRubyRuntimePool extends ManagedPool<Ruby> implements RubyRuntimePool, DefaultRubyRuntimePoolMBean {

    private String name;

    /**
     * Construct with a factory.
     * 
     * @param factory
     *            The factory to create interpreters.
     */
    public DefaultRubyRuntimePool(RubyRuntimeFactory factory) {
        super( factory );
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
     * Retrieve the interpreter factory.
     * 
     * @return The interpreter factory.
     */
    public RubyRuntimeFactory getRubyRuntimeFactory() {
        return (RubyRuntimeFactory) getInstanceFactory();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
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
