package org.torquebox.interp.deployers;

import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimeFactory;

public class DeployerRuby {

    private RubyRuntimeFactory factory;
    private Ruby ruby;

    public DeployerRuby(RubyRuntimeFactory factory) {
        this.factory = factory;
    }

    public DeployerRuby(Ruby ruby) {
        this.ruby = ruby;
    }

    public RubyRuntimeFactory getRubyRuntimeFactoyr() {
        return this.factory;
    }

    public synchronized Ruby getRuby() throws Exception {
        if (this.ruby == null) {
            this.ruby = this.factory.create();
        }

        return this.ruby;
    }

}
