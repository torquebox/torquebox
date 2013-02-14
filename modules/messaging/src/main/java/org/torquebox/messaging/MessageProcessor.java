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

package org.torquebox.messaging;

import javax.jms.Message;

import org.hornetq.api.core.client.ClientMessage;
import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.projectodd.polyglot.messaging.BaseMessageProcessor;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.messaging.component.MessageProcessorComponent;

public class MessageProcessor extends BaseMessageProcessor {

    @Override
    public void onMessage(Message message) {
        if (getConsumer() == null) {
            return; // racist!
        }
        MessageProcessorGroup group = (MessageProcessorGroup) getGroup();
        try {
            ensureCurrentRuntime();
            if (getConsumer() == null) {
                return; // racist!
            }

            MessageProcessorComponent component = (MessageProcessorComponent) group.getComponentResolver().resolve( this.currentRuby );
            try {
                component.process( message, getSession(), group );
                if (isXAEnabled()) {
                    log.trace( "Committing XA transaction for messageId " + message.getJMSMessageID() );
                    commitXATransaction();
                } else {
                    log.trace( "Committing non-XA transaction for messageId " + message.getJMSMessageID() );
                    getSession().commit();
                }
            } catch (Throwable e) {
                if (isXAEnabled()) {
                    log.trace( "Rolling back XA transaction for messageId " + message.getJMSMessageID() );
                    rollbackXATransaction( e );
                } else {
                    log.trace( "Rolling back non-XA transaction for messageId " + message.getJMSMessageID() );
                    getSession().rollback();
                    throw e;
                }
            }
        } catch (Throwable e) {
            log.error( "Unexpected error in " + group.getName(), e );
        } finally {
            clearCurrentRuntime();
        }
    }

    @Override
    protected void prepareTransaction() {
        try {
            ensureCurrentRuntime();
            RuntimeHelper.call( this.currentRuby, this.currentRubyTM, "prepare", new Object[] {} );
            RuntimeHelper.call( this.currentRuby, this.currentRubyTM, "enlist", new Object[] { getSession() } );
        } catch (Exception e) {
            log.error( "Failed to prepare transaction for message", e );
        }
    }

    protected void rollbackXATransaction(Throwable e) {
        if (this.currentRuby != null && this.currentRubyTM != null) {
            RuntimeHelper.call( this.currentRuby, this.currentRubyTM, "error", new Object[] { e } );
        }
    }

    protected void commitXATransaction() {
        if (this.currentRuby != null && this.currentRubyTM != null) {
            RuntimeHelper.call( this.currentRuby, this.currentRubyTM, "commit", new Object[] {} );
        }
    }

    protected void ensureCurrentRuntime() throws Exception {
        if (this.currentRuby == null) {
            this.currentRuby = ((MessageProcessorGroup) getGroup()).getRubyRuntimePool().borrowRuntime( getGroup().getName() );
            this.currentRubyTM = RuntimeHelper.invokeClassMethod( this.currentRuby, "TorqueBox::Transactions::Manager", "current", new Object[] {} );
        }
    }

    protected void clearCurrentRuntime() {
        if (this.currentRuby != null) {
            try {
                RuntimeHelper.evalScriptlet( this.currentRuby, "Thread.current[:torquebox_transaction] = nil" );
                ((MessageProcessorGroup) getGroup()).getRubyRuntimePool().returnRuntime( this.currentRuby );
            } catch (Throwable e) {
                log.warn( "Possible memory leak?", e );
            }
        }
        this.currentRuby = null;
        this.currentRubyTM = null;
    }

    public static final Logger log = Logger.getLogger( "org.torquebox.messaging" );

    private Ruby currentRuby = null;
    private Object currentRubyTM = null;
}
