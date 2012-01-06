/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.messaging;

import javax.jms.Message;

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
