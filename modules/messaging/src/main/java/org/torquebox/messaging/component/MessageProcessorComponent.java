/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.messaging.component;

import javax.jms.Message;
import javax.jms.Session;

import org.jruby.RubyModule;
import org.torquebox.core.component.AbstractRubyComponent;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.messaging.MessageProcessorGroup;

public class MessageProcessorComponent extends AbstractRubyComponent {

    public MessageProcessorComponent() {

    }

    public void process(Message message, Session session, MessageProcessorGroup group) {
        try {
            RubyModule messageWrapperClass = getClass( "TorqueBox::Messaging::Message" );
            Object wrappedMessage = _callRubyMethod( messageWrapperClass, "new", message );
            _callRubyMethodIfDefined("initialize_proxy", group);
            _callRubyMethod( findMiddleware(), "invoke", session, wrappedMessage, getRubyComponent() );
        } finally {
            RuntimeHelper.evalScriptlet( getRuby(), "ActiveRecord::Base.clear_active_connections! if defined?(ActiveRecord::Base)" );
        }
    }
    
    protected Object findMiddleware() {
        Object middleware = _callRubyMethodIfDefined( "middleware" );
        if (middleware != null) {
            return middleware;
        } else {
            // This will only be the case when a processor that doesn't extend MessageProcessor or include DefaultMiddleware
            // is provided.
            return _callRubyMethod( getClass( "TorqueBox::Messaging::ProcessorMiddleware::DefaultMiddleware" ),
                    "default" );
        }
    }

}
