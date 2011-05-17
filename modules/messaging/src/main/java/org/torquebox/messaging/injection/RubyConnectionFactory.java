package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.core.injection.ConvertableRubyInjection;

public class RubyConnectionFactory implements ConvertableRubyInjection {

    public RubyConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    @Override
    public Object convert(Ruby ruby) throws Exception {
        ruby.evalScriptlet( "require %q(torquebox-messaging)" );
        RubyModule connectionFactoryClass = ruby.getClassFromPath( "TorqueBox::Messaging::ConnectionFactory" );
        Object destination = JavaEmbedUtils.invokeMethod( ruby, connectionFactoryClass, "new", new Object[] { this.connectionFactory }, Object.class );
        return destination;

    }
    
    private ConnectionFactory connectionFactory;

}
