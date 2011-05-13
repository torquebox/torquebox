package org.torquebox.core.component;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class ComponentResolverService implements Service<ComponentResolver> {

    public ComponentResolverService(ComponentResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    public ComponentResolver getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resolver;
    }

    @Override
    public void start(StartContext context) throws StartException {
        
    }

    @Override
    public void stop(StopContext context) {
        
    }
    
    private ComponentResolver resolver;
}
