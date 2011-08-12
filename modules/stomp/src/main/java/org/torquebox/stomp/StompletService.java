package org.torquebox.stomp;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.container.SimpleStompletContainer;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.stomp.component.XAStompletComponent;

public class StompletService implements Service<Stomplet> {

    public StompletService() {

    }

    public void setDestinationPattern(String destinationPattern) {
        this.destinationPattern = destinationPattern;
    }

    public String getDestinationPattern() {
        return this.destinationPattern;
    }

    @Override
    public Stomplet getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(StartContext context) throws StartException {

        try {
            this.runtime = this.poolInjector.getValue().borrowRuntime();

            try {
                ComponentResolver componentResolver = this.componentResolverInjector.getValue();
                XAStompletComponent stomplet = (XAStompletComponent) componentResolver.resolve( runtime );

                SimpleStompletContainer container = containerInjector.getValue();
                container.addStomplet( this.destinationPattern, stomplet );
            } catch (Exception e) {
                this.poolInjector.getValue().returnRuntime( this.runtime );
                this.runtime = null;
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.failed( new StartException( e ) );
        }

    }

    @Override
    public void stop(StopContext context) {
        this.poolInjector.getValue().returnRuntime( this.runtime );
        this.runtime = null;
    }

    public Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.poolInjector;
    }
    
    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }
    
    public Injector<SimpleStompletContainer> getStompletContainerInjector() {
        return this.containerInjector;
    }
    
    private InjectedValue<RubyRuntimePool> poolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<SimpleStompletContainer> containerInjector = new InjectedValue<SimpleStompletContainer>();

    private Ruby runtime;

    private String destinationPattern;
}
