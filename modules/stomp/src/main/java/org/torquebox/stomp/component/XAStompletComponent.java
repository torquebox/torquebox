package org.torquebox.stomp.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAResource;

import org.jruby.RubyArray;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.spi.StompSession;
import org.projectodd.stilts.stomplet.StompletConfig;
import org.projectodd.stilts.stomplet.Subscriber;
import org.projectodd.stilts.stomplet.XAStomplet;
import org.projectodd.stilts.stomplet.container.SubscriberImpl;
import org.projectodd.stilts.stomplet.container.xa.PseudoXAStompletAcknowledgeableMessageSink;
import org.projectodd.stilts.stomplet.container.xa.PseudoXAStompletResourceManager;
import org.projectodd.stilts.stomplet.container.xa.PseudoXAStompletTransaction;
import org.torquebox.core.component.AbstractRubyComponent;

public class XAStompletComponent extends AbstractRubyComponent implements XAStomplet {

    public XAStompletComponent() {
        this.stomplet = new DirectStompletComponent( this );
    }

    @Override
    public void initialize(StompletConfig config) throws StompException {
        this.stomplet.initialize( config );
        Object isXa = _callRubyMethod( getRubyComponent(), "respond_to?", "xa_resources" );

        if (isXa == Boolean.TRUE) {
            RubyArray result = (RubyArray) _callRubyMethodIfDefined( "xa_resources" );
            if (result != null) {
                for (Object resource : result) {
                    this.xaResources.add( (XAResource) resource );
                }
            }
        } else {
            this.resourceManager = new PseudoXAStompletResourceManager( this.stomplet );
            this.xaResources.add( this.resourceManager );
        }
    }

    @Override
    public void destroy() throws StompException {
        this.stomplet.destroy();
    }

    @Override
    public void onMessage(StompMessage message, StompSession session) throws StompException {
        PseudoXAStompletTransaction tx = null;
        
        if (this.resourceManager != null) {
            tx = this.resourceManager.currentTransaction();
        }
        
        if (tx == null) {
            this.stomplet.onMessage( message, session );
        } else {
            tx.addSentMessage( message, session );
        }
    }

    @Override
    public void onSubscribe(Subscriber subscriber) throws StompException {
        String subscriberId = subscriber.getId();
        Subscriber xaSubscriber = new SubscriberImpl( subscriber.getSession(), stomplet, subscriberId, subscriber.getDestination(), new PseudoXAStompletAcknowledgeableMessageSink(
                this.resourceManager, subscriber ),
                subscriber.getAckMode() );
        this.subscribers.put( subscriberId, xaSubscriber );
        this.stomplet.onSubscribe( xaSubscriber );
    }

    @Override
    public void onUnsubscribe(Subscriber subscriber) throws StompException {
        Subscriber xaSubscriber = this.subscribers.remove( subscriber.getId() );
        if (xaSubscriber != null) {
            this.stomplet.onUnsubscribe( xaSubscriber );
        }
    }

    @Override
    public Set<XAResource> getXAResources() {
        return this.xaResources;
    }

    private PseudoXAStompletResourceManager resourceManager;
    private DirectStompletComponent stomplet;
    private Map<String, Subscriber> subscribers = new HashMap<String, Subscriber>();
    private Set<XAResource> xaResources = new HashSet<XAResource>();

}
