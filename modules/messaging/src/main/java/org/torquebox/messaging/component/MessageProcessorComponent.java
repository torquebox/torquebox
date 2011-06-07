/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
