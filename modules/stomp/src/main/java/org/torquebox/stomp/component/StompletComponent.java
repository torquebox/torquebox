package org.torquebox.stomp.component;

import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;
import org.torquebox.core.component.AbstractRubyComponent;

public class StompletComponent extends AbstractRubyComponent implements Stomplet {

    @Override
    public void initialize(StompletConfig config) throws StompException {
        _callRubyMethod( "configure", config );
    }

    @Override
    public void destroy() throws StompException {
        _callRubyMethod( "destroy" );
    }

    @Override
    public void onMessage(StompMessage message) throws StompException {
        _callRubyMethod( "on_message", message );
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        _callRubyMethod( "on_subscribe", subscriber );
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        _callRubyMethod( "on_unsubscribe", subscriber );
    }

}
