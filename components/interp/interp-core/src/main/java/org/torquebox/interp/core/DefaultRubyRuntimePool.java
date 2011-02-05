/* Copyright 2010 Red Hat, Inc. */
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
public class DefaultRubyRuntimePool extends ManagedPool<Ruby> implements RubyRuntimePool {

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

}
