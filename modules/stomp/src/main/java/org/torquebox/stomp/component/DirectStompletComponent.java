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

package org.torquebox.stomp.component;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.spi.StompSession;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;
import org.torquebox.core.util.RuntimeHelper;

public class DirectStompletComponent implements Stomplet {

    public DirectStompletComponent(XAStompletComponent component) {
        this.component = component;
    }

    @Override
    public void initialize(StompletConfig config) throws StompException {
        this.component._callRubyMethodIfDefined( "configure", config );
    }

    @Override
    public void destroy() throws StompException {
        try {
            this.component._callRubyMethodIfDefined( "destroy" );
        } catch (Exception e) {
            e.printStackTrace();
            throw new StompException( e );
        }
    }

    @Override
    public void onMessage(StompMessage message, StompSession session) throws StompException {
        loadSessionData( session );
        try {
            this.component._callRubyMethodIfDefined( "on_message", message, session );
        } finally {
            storeSessionData( session );
        }
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        loadSessionData( subscriber.getSession() );
        try {
            this.component._callRubyMethodIfDefined( "on_subscribe", subscriber );
        } finally {
            storeSessionData( subscriber.getSession() );
        }
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        loadSessionData( subscriber.getSession() );
        try {
            this.component._callRubyMethodIfDefined( "on_unsubscribe", subscriber );
        } finally {
            storeSessionData( subscriber.getSession() );
        }
    }

    protected void loadSessionData(StompSession session) {
        session.access();
        Ruby ruby = this.component.getRubyComponent().getRuntime();
        RuntimeHelper.callIfPossible( ruby, session, "load_session_data", EMPTY_OBJECT_ARRAY );
    }

    protected void storeSessionData(StompSession session) {
        try {
            Ruby ruby = this.component.getRubyComponent().getRuntime();
            RuntimeHelper.callIfPossible( ruby, session, "store_session_data", EMPTY_OBJECT_ARRAY );
        } finally {
            session.endAccess();
        }
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = {};
    private XAStompletComponent component;

}
