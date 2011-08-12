package org.torquebox.stomp.component;

import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;

public class DirectStompletComponent implements Stomplet {

    public DirectStompletComponent(XAStompletComponent component) {
        this.component = component;
    }
    
    @Override
    public void initialize(StompletConfig config) throws StompException {
        this.component._callRubyMethod( "configure", config );
    }

    @Override
    public void destroy() throws StompException {
        this.component._callRubyMethod( "destroy" );
    }

    @Override
    public void onMessage(StompMessage message) throws StompException {
        this.component._callRubyMethod( "on_message", message );
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        this.component._callRubyMethod( "on_subscribe", subscriber );
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        this.component._callRubyMethod( "on_unsubscribe", subscriber );
    }
    
    private XAStompletComponent component;

}
