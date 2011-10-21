package org.torquebox.core.datasource;

import java.sql.Driver;

import org.jboss.as.connector.registry.DriverRegistry;
import org.jboss.as.connector.registry.InstalledDriver;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.torquebox.core.datasource.db.Adapter;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.util.RuntimeHelper;

public class DriverService implements Service<Driver> {

    public DriverService(String applicationDirectory, Adapter adapter) {
        this.applicationDirectory = applicationDirectory;
        this.adapter = adapter;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        context.execute( new Runnable() {
            public void run() {
                try {
                    DriverService.this.driver = instantiateDriver();
                    log.debug( "driver: " + DriverService.this.driver );
                    DriverService.this.installedDriver = createInstalledDriver();

                    DriverRegistry registry = DriverService.this.driverRegistryInjector.getValue();
                    registry.registerInstalledDriver( installedDriver );
                    
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        } );

    }

    @Override
    public void stop(StopContext context) {
        this.driverRegistryInjector.getValue().unregisterInstalledDriver( this.installedDriver );
    }

    protected Driver instantiateDriver() throws Exception {
        Ruby ruby = this.runtimeInjector.getValue();

        synchronized (ruby) {
            ruby.setCurrentDirectory( this.applicationDirectory );

            RuntimeHelper.require( ruby, "bundler/setup" );
            RuntimeHelper.require( ruby, this.adapter.getRequirePath() );

            ClassLoader classLoader = ruby.getJRubyClassLoader();
            final Class<? extends Driver> driverClass = classLoader.loadClass( this.adapter.getDriverClassName() ).asSubclass( Driver.class );
            Driver driver = driverClass.newInstance();
            return driver;
        }
    }

    protected InstalledDriver createInstalledDriver() {
        int majorVersion = this.driver.getMajorVersion();
        int minorVersion = this.driver.getMinorVersion();
        boolean compliant = this.driver.jdbcCompliant();
        return new InstalledDriver( this.adapter.getId(), this.driver.getClass()
                .getName(), null, null, majorVersion, minorVersion, compliant );
    }

    @Override
    public Driver getValue() throws IllegalStateException,
            IllegalArgumentException {
        return this.driver;
    }

    public Injector<Ruby> getRuntimeInjector() {
        return this.runtimeInjector;
    }

    public Injector<RubyRuntimeFactory> getRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }

    public Injector<DriverRegistry> getDriverRegistryInjector() {
        return this.driverRegistryInjector;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.db" );

    private InjectedValue<Ruby> runtimeInjector = new InjectedValue<Ruby>();
    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();

    private InjectedValue<DriverRegistry> driverRegistryInjector = new InjectedValue<DriverRegistry>();

    private String applicationDirectory;
    private Adapter adapter;

    private Driver driver;
    private InstalledDriver installedDriver;

}
