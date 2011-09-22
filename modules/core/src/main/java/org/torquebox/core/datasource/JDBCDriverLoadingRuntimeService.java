package org.torquebox.core.datasource;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class JDBCDriverLoadingRuntimeService implements Service<Ruby> {

    @Override
    public Ruby getValue() throws IllegalStateException, IllegalArgumentException {
        return this.runtime;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.runtime = this.runtimeFactoryInjector.getValue().createInstance( "JDBC Driver Loader", false );
        } catch (IllegalStateException e) {
            throw new StartException( e);
        } catch (Exception e) {
            throw new StartException( e);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.runtimeFactoryInjector.getValue().destroyInstance( this.runtime );
        this.runtime = null;
    }
    
    public Injector<RubyRuntimeFactory> getRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }
    
    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();
    
    private Ruby runtime;


}
