package org.torquebox.stomp.component;

import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.spi.StompSession;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;

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
        this.component._callRubyMethodIfDefined( "destroy" );
    }

    @Override
    public void onMessage(StompMessage message, StompSession session) throws StompException {
        session.access();
        try {
            this.component._callRubyMethodIfDefined( "on_message", message, session );
        } finally {
            session.endAccess();
        }
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        subscriber.getSession().access();
        try {
            this.component._callRubyMethodIfDefined( "on_subscribe", subscriber );
        } finally {
            subscriber.getSession().endAccess();
        }
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        subscriber.getSession().access();
        try {
            this.component._callRubyMethodIfDefined( "on_unsubscribe", subscriber );
        } finally {
            subscriber.getSession().endAccess();
        }
    }

    private XAStompletComponent component;

}
