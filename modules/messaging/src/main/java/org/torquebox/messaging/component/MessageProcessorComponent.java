package org.torquebox.messaging.component;

import javax.jms.Message;

import org.jruby.RubyModule;
import org.torquebox.core.component.AbstractRubyComponent;

public class MessageProcessorComponent extends AbstractRubyComponent {

    public MessageProcessorComponent() {

    }

    public void process(Message message) {
        //require( "torquebox/messaging/message_processor");
        //require( "torquebox/messaging/task");
        RubyModule messageWrapperClass = getClass( "TorqueBox::Messaging::Message" );
        Object wrappedMessage = __call__( messageWrapperClass, "new", message );
        __call__( "process!", wrappedMessage );
    }

}
