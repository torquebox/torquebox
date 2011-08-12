package org.torquebox.stomp.component;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.xa.XAResource;

import org.jruby.RubyArray;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;
import org.projectodd.stilts.stomplet.XAStomplet;
import org.torquebox.core.component.AbstractRubyComponent;

public class StompletComponent extends AbstractRubyComponent implements XAStomplet {

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

    @Override
    public Set<XAResource> getXAResources() {
        Set<XAResource> xaResources = new HashSet<XAResource>();
        Object isXa = _callRubyMethod( getRubyComponent(), "respond_to?", "xa_resources" );
        
        if (isXa == Boolean.TRUE) {
            RubyArray result = (RubyArray) _callRubyMethodIfDefined( "xa_resources" );
            for ( Object resource : result ) {
                xaResources.add( (XAResource) resource );
            }
        }
        
        return xaResources;
    }
}
