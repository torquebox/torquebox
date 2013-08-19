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
            clearActiveRecordConnections();
            storeSessionData( session );
        }
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        loadSessionData( subscriber.getSession() );
        try {
            this.component._callRubyMethodIfDefined( "on_subscribe", subscriber );
        } finally {
            clearActiveRecordConnections();
            storeSessionData( subscriber.getSession() );
        }
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        loadSessionData( subscriber.getSession() );
        try {
            this.component._callRubyMethodIfDefined( "on_unsubscribe", subscriber );
        } finally {
            clearActiveRecordConnections();
            storeSessionData( subscriber.getSession() );
        }
    }

    protected void loadSessionData(StompSession session) {
        session.access();
        RuntimeHelper.callIfPossible( getRuby(), session, "load_session_data", EMPTY_OBJECT_ARRAY );
    }

    protected void storeSessionData(StompSession session) {
        try {
            RuntimeHelper.callIfPossible( getRuby(), session, "store_session_data", EMPTY_OBJECT_ARRAY );
        } finally {
            session.endAccess();
        }
    }

    protected Ruby getRuby() {
        return this.component.getRubyComponent().getRuntime();
    }

    protected void clearActiveRecordConnections() {
        RuntimeHelper.evalScriptlet( getRuby(), "ActiveRecord::Base.clear_active_connections! if defined?(ActiveRecord::Base)" );
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = {};
    private XAStompletComponent component;

}
