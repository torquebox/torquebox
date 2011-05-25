package org.torquebox.jobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.msc.service.BatchServiceTarget;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.Value;

public class MockServiceTarget implements ServiceTarget {
    
    private MockServiceTarget parent;
    
    private Set<ServiceListener<Object>> listeners = new HashSet<ServiceListener<Object>>();
    private Set<ServiceName> dependencies = new HashSet<ServiceName>();

    private Map<ServiceName,MockServiceBuilder<?>> services = new HashMap<ServiceName,MockServiceBuilder<?>>();
    
    public MockServiceTarget() {
        
    }
    
    public MockServiceTarget(MockServiceTarget parent) {
        this.parent = parent;
    }
    
    public Collection<MockServiceBuilder<?>> getMockServiceBuilders() {
        return this.services.values();
    }
    
    public MockServiceBuilder<?> getMockServiceBuilder(ServiceName name) {
        return this.services.get(  name  );
    }
    
    void install(MockServiceBuilder<?> builder) {
        this.services.put(  builder.getServiceName(), builder );
    }
    
    @Override
    public <T> ServiceBuilder<T> addServiceValue(ServiceName name, Value<? extends Service<T>> value) {
        return new MockServiceBuilder<T>( this, name, value );
    }

    @Override
    public <T> ServiceBuilder<T> addService(ServiceName name, Service<T> service) {
        return new MockServiceBuilder<T>( this, name, service );
    }

    @Override
    public ServiceTarget addListener(ServiceListener<Object> listener) {
        this.listeners.add( listener );
        return this;
    }

    @Override
    public ServiceTarget addListener(ServiceListener<Object>... listeners) {
        for ( ServiceListener<Object> each : listeners ) {
            addListener( each );
        }
        return this;
    }

    @Override
    public ServiceTarget addListener(Collection<ServiceListener<Object>> listeners) {
        this.listeners.addAll( listeners );
        return this;
    }

    @Override
    public ServiceTarget removeListener(ServiceListener<Object> listener) {
        this.listeners.remove(  listener );
        return this;
    }

    @Override
    public Set<ServiceListener<Object>> getListeners() {
        return this.listeners;
    }

    @Override
    public ServiceTarget addDependency(ServiceName dependency) {
        this.dependencies.add(  dependency  );
        return this;
    }

    @Override
    public ServiceTarget addDependency(ServiceName... dependencies) {
        for ( ServiceName each : dependencies ) {
            addDependency( each );
        }
        return this;
    }

    @Override
    public ServiceTarget addDependency(Collection<ServiceName> dependencies) {
        this.dependencies.addAll( dependencies );
        return this;
    }

    @Override
    public ServiceTarget removeDependency(ServiceName dependency) {
        this.dependencies.remove( dependency );
        return this;
    }

    @Override
    public Set<ServiceName> getDependencies() {
        return this.dependencies;
    }

    @Override
    public ServiceTarget subTarget() {
        return new MockServiceTarget( this );
    }

    @Override
    public BatchServiceTarget batchTarget() {
        return null;
    }

}
