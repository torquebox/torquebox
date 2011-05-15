package org.torquebox.core.injection;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class ConvertibleInjectableService implements Service<Object> {

    public ConvertibleInjectableService(InjectableConverter converter) {
        this.converter = converter;
    }
    
    @Override
    public Object getValue() throws IllegalStateException, IllegalArgumentException {
        return this.wrapped;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.wrapped = this.converter.wrap( objectInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.wrapped = null;
    }
    
    public Injector<Object> getObjectInjector() {
        return this.objectInjector;
    }

    private InjectedValue<Object> objectInjector = new InjectedValue<Object>();
    private InjectableConverter converter;
    private Object wrapped;
}
