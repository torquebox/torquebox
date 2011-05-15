package org.torquebox.messaging.injection;

import javax.jms.Queue;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.core.injection.ConvertableRubyInjection;

public class ConvertibleQueue implements ConvertableRubyInjection {

    public ConvertibleQueue(Queue queue) {
        System.err.println( "ConvertibleQueue for " + queue + " // " + queue.getClass() );
        this.queue = queue;
    }
    
    @Override
    public Object convert(Ruby ruby) throws Exception {
        ruby.evalScriptlet( "require %q(torquebox-messaging)" );
        RubyModule destinationClass = ruby.getClassFromPath( "TorqueBox::Messaging::Queue" );
        Object destination = JavaEmbedUtils.invokeMethod( ruby, destinationClass, "new", new Object[] { this.queue.getQueueName() }, Object.class );
        return destination;
    }
    
    private Queue queue;


}
