package org.torquebox.messaging.component;

import javax.jms.Message;

import org.jruby.RubyModule;
import org.torquebox.core.component.AbstractRubyComponent;

public class MessageProcessorComponent extends AbstractRubyComponent {

    public MessageProcessorComponent() {

    }

    public void process(Message message) {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( getRuby().getJRubyClassLoader().getParent() );
            RubyModule messageWrapperClass = getClass( "TorqueBox::Messaging::Message" );
            Object wrappedMessage = _callRubyMethod( messageWrapperClass, "new", message );
            _callRubyMethod( "process!", wrappedMessage );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }

}
