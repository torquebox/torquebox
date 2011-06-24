/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.test.as;

import java.util.Collection;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceListener.Inheritance;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
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
    public ServiceBuilder<T> addDependencies(Iterable<ServiceName> dependencies) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependencies(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, Iterable<ServiceName> dependencies) {
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
    public ServiceBuilder<T> addDependency(ServiceName dependency, Injector<Object> target) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addDependency(org.jboss.msc.service.ServiceBuilder.DependencyType dependencyType, ServiceName dependency, Injector<Object> target) {
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

    @Override
    public ServiceBuilder<T> addListener(Inheritance inheritance, ServiceListener<? super T> listener) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addListener(Inheritance inheritance, ServiceListener<? super T>... listeners) {
        return this;
    }

    @Override
    public ServiceBuilder<T> addListener(Inheritance inheritance, Collection<? extends ServiceListener<? super T>> listeners) {
        return this;
    }

}
