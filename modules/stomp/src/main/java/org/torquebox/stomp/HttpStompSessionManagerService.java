package org.torquebox.stomp;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.stilts.conduit.spi.StompSessionManager;

public class HttpStompSessionManagerService implements Service<StompSessionManager> {

    @Override
    public StompSessionManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.sessionManager;
    }

    @Override
    public void start(StartContext context) throws StartException {
        Manager webSessionManager = this.contextInjector.getValue().getManager();
        System.err.println( "manager=" + webSessionManager );
        this.sessionManager = new HttpStompSessionManager( webSessionManager );
    }

    @Override
    public void stop(StopContext context) {
        this.sessionManager = null;
    }
    
    public Injector<Context> getContextInjector() {
        return this.contextInjector;
    }
    
    private InjectedValue<Context> contextInjector = new InjectedValue<Context>();
    
    
    private HttpStompSessionManager sessionManager;

}
