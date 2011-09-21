package org.torquebox.core.datasource;

import java.sql.Driver;

import org.jboss.as.connector.registry.DriverRegistry;
import org.jboss.as.connector.registry.InstalledDriver;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.util.RuntimeHelper;

public class DriverService implements Service<Driver> {

    public DriverService(String applicationDirectory, String driverName, String driverClassName) {
        this.applicationDirectory = applicationDirectory;
        this.driverName = driverName;
        this.driverClassName = driverClassName;
    }

    @Override
    public void start(StartContext context) throws StartException {

        try {
            this.driver = instantiateDriver();
            InstalledDriver installedDriver = createInstalledDriver();
            
            DriverRegistry registry = this.driverRegistryInjector.getValue();
            
            registry.registerInstalledDriver( installedDriver );
        } catch (Exception e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    protected Driver instantiateDriver() throws Exception {
        RubyRuntimeFactory factory = runtimeFactoryInjector.getValue();
        Ruby ruby = factory.createInstance( "JDBC Driver Loader", false );
        
        ruby.setCurrentDirectory( this.applicationDirectory );

        RuntimeHelper.require( ruby, "bundler/setup" );
        RuntimeHelper.require( ruby, "jdbc/" + this.driverName );

        try {
            ClassLoader classLoader = ruby.getJRubyClassLoader();
            final Class<? extends Driver> driverClass = classLoader.loadClass( this.driverClassName ).asSubclass( Driver.class );
            Driver driver = driverClass.newInstance();
            return driver;
        } finally {
            factory.destroyInstance( ruby );
        }
    }

    protected InstalledDriver createInstalledDriver() {
        int majorVersion = this.driver.getMajorVersion();
        int minorVersion = this.driver.getMinorVersion();
        boolean compliant = this.driver.jdbcCompliant();
        return new InstalledDriver( this.driverName, this.driver.getClass().getName(), null, null, majorVersion, minorVersion, compliant );
    }

    @Override
    public Driver getValue() throws IllegalStateException, IllegalArgumentException {
        return this.driver;
    }

    public Injector<RubyRuntimeFactory> getRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }
    
    public Injector<DriverRegistry> getDriverRegistryInjector() {
        return this.driverRegistryInjector;
    }

    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();
    private InjectedValue<DriverRegistry> driverRegistryInjector = new InjectedValue<DriverRegistry>();

    private String applicationDirectory;
    private String driverName;
    private String driverClassName;
    
    private Driver driver;

}
