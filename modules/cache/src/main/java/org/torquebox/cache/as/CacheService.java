package org.torquebox.cache.as;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.manager.CacheContainer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class CacheService implements Service<CacheService> {

    private boolean clustered = false;
    private CacheContainer container;

    @Override
    public CacheService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            InitialContext ic = new InitialContext();
            container = (CacheContainer) ic.lookup( this.infinispanServiceName() );
            if (container != null) {
                container.start();
            } else {
                System.err.println( "ERROR: Can't find ye olde cache container" );
            }
        } catch (NamingException e) {
            // TODO
            System.err.println( "ERROR: Cannot get cache container." );
        }
    }

    @Override
    public void stop(StopContext context) {
        // NOT SURE ABOUT THIS
        // It seems like the service can be stopped while there are still apps
        // that
        // depend on it. So...
        // if (container != null) { container.stop(); }
    }

    public CacheContainer getCacheContainer() {
        return this.container;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isClustered() {
        return this.clustered;
    }

    private String infinispanServiceName() {
        if (this.isClustered()) {
            return "java:jboss/infinispan/container/web";
        } else {
            return "java:jboss/infinispan/container/polyglot";
        }
    }

}
