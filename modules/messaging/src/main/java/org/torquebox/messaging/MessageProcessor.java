package org.torquebox.messaging;

import javax.jms.Message;
import javax.transaction.xa.XAException;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.projectodd.polyglot.messaging.BaseMessageProcessor;
import org.torquebox.messaging.component.MessageProcessorComponent;

public class MessageProcessor extends BaseMessageProcessor {

    @Override
    public void onMessage(Message message) {
        if (getConsumer() == null) {
            return; // racist!
        }
        MessageProcessorGroup group = (MessageProcessorGroup) getGroup();
        Ruby ruby = null;
        try {
            ruby = group.getRubyRuntimePool().borrowRuntime( getGroup().getName() );
            if (getConsumer() == null) {
                return; // racist!
            }
            MessageProcessorComponent component = (MessageProcessorComponent) group.getComponentResolver().resolve( ruby );
            component.process( message, getSession() );
        } catch (Exception e) {
            log.error( "Unexpected error in " + group.getName(), e );
        } finally {
            if (ruby != null) {
                try {
                    group.getRubyRuntimePool().returnRuntime( ruby );
                } catch (Throwable e) {
                    log.warn( "Possible memory leak?", e );
                }
            }
        }

    }

    public static final Logger log = Logger.getLogger( "org.torquebox.messaging" );
}
