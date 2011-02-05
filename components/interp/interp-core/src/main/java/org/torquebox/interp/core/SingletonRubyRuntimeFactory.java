package org.torquebox.interp.core;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimeFactory;

public class SingletonRubyRuntimeFactory implements RubyRuntimeFactory {

    private Ruby ruby;

    public SingletonRubyRuntimeFactory() {

    }

    public void setRuby(Ruby ruby) {
        this.ruby = ruby;
    }

    public Ruby getRuby() {
        return this.ruby;
    }

    @Override
    public Ruby create() throws Exception {
        return getRuby();
    }

    @Override
    public void dispose(Ruby instance) {
        // no-op, we didn't create the ruby.
    }

    public CompatVersion getRubyVersion() {
        if (this.ruby.is1_9()) {
            return CompatVersion.RUBY1_9;
        }

        return CompatVersion.RUBY1_8;
    }

}
