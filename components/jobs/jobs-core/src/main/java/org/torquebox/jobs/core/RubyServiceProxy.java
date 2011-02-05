package org.torquebox.jobs.core;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.interp.core.RubyComponentResolver;

public class RubyServiceProxy {

    public RubyServiceProxy() {
        // MicroContainer seems to want this declared
    }

    public RubyServiceProxy(RubyComponentResolver resolver, RubyRuntimePool pool) {
        setRubyComponentResolver( resolver );
        setRubyRuntimePool( pool );
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public void setRubyComponentResolver(RubyComponentResolver resolver) {
        this.resolver = resolver;
    }

    public void start() throws Exception {
        if (this.ruby != null)
            throw new IllegalStateException( "Already running" );
        this.ruby = runtimePool.borrowRuntime();
        ReflectionHelper.callIfPossible( this.ruby, getService(), "start", null );
    }

    public void stop() throws Exception {
        if (this.ruby == null)
            throw new IllegalStateException( "Not running" );
        ReflectionHelper.callIfPossible( this.ruby, getService(), "stop", null );
        runtimePool.returnRuntime( this.ruby );
        this.ruby = null;
    }

    protected IRubyObject getService() throws Exception {
        if (this.service == null) {
            this.service = resolver.resolve( this.ruby );
        }
        return this.service;
    }

    private RubyRuntimePool runtimePool;
    private Ruby ruby;
    private IRubyObject service;
    private RubyComponentResolver resolver;
}
