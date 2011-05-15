package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.core.injection.ConvertableRubyInjection;

public class LiveDestination implements ConvertableRubyInjection {

    public LiveDestination(ConnectionFactory connectionFactory, Destination destination) {
        this.connectionFactory = connectionFactory;
        this.destination = destination;
    }
    
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
    
    public Destination getDestination() {
        return this.destination;
    }
    
    @Override
    public Object convert(Ruby ruby) throws Exception {
        ruby.evalScriptlet( "require %q(torquebox-messaging)" );
        RubyModule destinationClass = ruby.getClassFromPath( "TorqueBox::Messaging::Core::Live" + getType() );
        Object destination = JavaEmbedUtils.invokeMethod( ruby, destinationClass, "new", new Object[] { this.connectionFactory, this.destination }, Object.class );
        return destination;
    }
    
    protected String getType() {
        if ( this.destination instanceof javax.jms.Queue ) {
            return "Queue";
        } else {
            return "Topic";
        }
    }
    
    private ConnectionFactory connectionFactory;
    private Destination destination;

}
