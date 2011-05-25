package org.torquebox.jobs;

import java.util.Collection;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Location;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.value.Value;

public class MockServiceBuilder<T> implements ServiceBuilder<T> {

    private MockServiceTarget serviceTarget;
    private ServiceName serviceName;
    private Value<?> value;

    public MockServiceBuilder(MockServiceTarget serviceTarget, ServiceName serviceName, Service<T> service) {
        this.serviceTarget = serviceTarget;
        this.serviceName = serviceName;
        this.value = service;
    }
    
    public MockServiceBuilder(MockServiceTarget serviceTarget, ServiceName serviceName, Value<? extends Service<T>> value) {
        this.serviceTarget = serviceTarget;
        this.serviceName = serviceName;
        this.value = value;
    }
    
    public ServiceName getServiceName() {
        return this.serviceName;
    }
    
    public Value<?> getValue() {
        return this.value;
    }

    @Override
    public ServiceBuilder<T> addAliases(ServiceName... aliases) {
        return this;
    }

    @Override
    public ServiceBuilder<T> setLocation() {
        return this;
    }

    @Override
    public ServiceBuilder<T> setLocation(Location location) {
        return this;
    }

    @Override
    public ServiceBuilder<T> setInitialMode(Mode mode) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependencies(ServiceName... dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependencies(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, ServiceName... dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addOptionalDependencies(ServiceName... dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependencies(Iterable<ServiceName> dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependencies(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, Iterable<ServiceName> dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addOptionalDependencies(Iterable<ServiceName> dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependency(ServiceName dependency) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependency(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, ServiceName dependency) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addOptionalDependency(ServiceName dependency) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependency(ServiceName dependency, Injector<Object> target) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependency(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, ServiceName dependency, Injector<Object> target) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addOptionalDependency(ServiceName dependency, Injector<Object> target) {
        return this;
    }

    @Override
    public <I> ServiceBuilder<T> addDependency(ServiceName dependency, Class<I> type, Injector<I> target) {
        return this;
    }

    @Override
    public <I> ServiceBuilder<T> addDependency(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, ServiceName dependency, Class<I> type,
            Injector<I> target) {
        return this;
    }

    @Override
    public <I> ServiceBuilder<T> addOptionalDependency(ServiceName dependency, Class<I> type, Injector<I> target) {
        return this;
    }

    @Override
    public <I> ServiceBuilder<T> addInjection(Injector<? super I> target, I value) {
        return this;
    }

    @Override
    public <I> ServiceBuilder<T> addInjectionValue(Injector<? super I> target, Value<I> value) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addInjection(Injector<? super T> target) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addListener(ServiceListener<? super T> listener) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addListener(ServiceListener<? super T>... listeners) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addListener(Collection<? extends ServiceListener<? super T>> listeners) {
        return this;
    }

    @Override
    public ServiceController<T> install() throws ServiceRegistryException, IllegalStateException {
        this.serviceTarget.install( this );
        return null;
    }

}
