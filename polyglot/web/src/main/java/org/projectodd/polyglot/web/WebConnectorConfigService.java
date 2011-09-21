package org.projectodd.polyglot.web;

import java.lang.reflect.Method;

import org.apache.catalina.connector.Connector;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class WebConnectorConfigService implements Service<WebConnectorConfigService> {

    public WebConnectorConfigService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public void start(StartContext context) throws StartException {
        if (this.maxThreads != null) {
            try {
                Connector connector = injectedConnector.getValue();
                Method m = connector.getProtocolHandler().getClass().getMethod( "setMaxThreads", int.class );
                m.invoke( connector.getProtocolHandler(), this.maxThreads );
            } catch (Exception e) {
                throw new StartException( e );
            }
        }
    }

    public void stop(StopContext context) {
        // nothing to do
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Injector<Connector> getConnectorInjector() {
        return injectedConnector;
    }

    private Integer maxThreads;
    private final InjectedValue<Connector> injectedConnector = new InjectedValue<Connector>();

}
