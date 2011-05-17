package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class RubyConnectionFactoryService implements Service<RubyConnectionFactory> {

    @Override
    public RubyConnectionFactory getValue() throws IllegalStateException, IllegalArgumentException {
        return this.rubyConnectionFactory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.rubyConnectionFactory = new RubyConnectionFactory( this.connectionFactoryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.rubyConnectionFactory = null;
    }
    
    public Injector<ConnectionFactory> getConnectionFactoryInjector() {
        return this.connectionFactoryInjector;
    }
    
    private RubyConnectionFactory rubyConnectionFactory;
    private InjectedValue<ConnectionFactory> connectionFactoryInjector = new InjectedValue<ConnectionFactory>();

}
