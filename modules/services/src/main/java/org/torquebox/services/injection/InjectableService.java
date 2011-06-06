package org.torquebox.services.injection;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.services.RubyService;

public class InjectableService implements Service<IRubyObject> {

    public InjectableService(RubyService service) {
        this.service = service;
    }

    @Override
    public IRubyObject getValue() throws IllegalStateException, IllegalArgumentException {
        return this.rubyService;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.rubyService = this.service.getComponent().getRubyComponent();
    }

    @Override
    public void stop(StopContext context) {
        this.rubyService = null;
    }

    private RubyService service;
    private IRubyObject rubyService;

}
