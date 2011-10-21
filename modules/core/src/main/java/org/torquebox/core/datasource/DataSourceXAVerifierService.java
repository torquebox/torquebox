package org.torquebox.core.datasource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.datasource.DataSourceInfoList.Info;

public class DataSourceXAVerifierService implements Service<DataSourceInfoList.Info> {

    public DataSourceXAVerifierService(Info info, ServiceRegistry serviceRegistry, String jndiName, ServiceName dataSourceServiceName) {
        this.info = info;
        this.serviceRegistry = serviceRegistry;
        this.jndiName = jndiName;
        this.dataSourceServiceName = dataSourceServiceName;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (!testSupportsXa()) {
            this.info = Info.DISABLED;
            removeBinder();
            removeReferenceFactory();
            removeDataSource();
        }

        log.info( "Verifier completed" );
    }

    private void removeBinder() {
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName);
        removeService( bindInfo.getBinderServiceName() );
    }
    
    private void removeReferenceFactory() {
        removeService( DataSourceReferenceFactoryService.SERVICE_NAME_BASE .append(jndiName) );
    }
    
    private void removeDataSource() {
        removeService( this.dataSourceServiceName );
    }
    
    private void removeService(ServiceName serviceName) {
        ServiceController<?> controller = this.serviceRegistry.getService( serviceName );
        if ( controller != null ) {
            controller.setMode( Mode.REMOVE );
        }
    }

    protected boolean testSupportsXa() {
        if ( this.info == Info.DISABLED ) {
            return false;
        }
        
        org.jboss.logmanager.Logger.getLogger( "com.arjuna.ats" ).setLevel( Level.OFF );
        try {
            DataSource dataSource = this.dataSourceInjector.getOptionalValue();
            if (dataSource == null) {
                return false;
            }
            TransactionManager tm = this.transactionManagerInjector.getValue();
            tm.begin();
            Transaction tx = tm.getTransaction();
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute( "SELECT 1;" );
            statement.close();
            tx.enlistResource( new DummyXAResource() );
            tm.commit();
        } catch (Exception e) {
            log.warnf( "Error performing probe of XA datasource; XA will not be enabled: %s", this.info.getName() );
            return false;
        }

        return true;
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public DataSourceInfoList.Info getValue() throws IllegalStateException, IllegalArgumentException {
        return this.info;
    }

    public Injector<DataSource> getDataSourceInjector() {
        return this.dataSourceInjector;
    }

    public Injector<TransactionManager> getTransactionManagerInjector() {
        return this.transactionManagerInjector;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.datasource" );
    private InjectedValue<DataSource> dataSourceInjector = new InjectedValue<DataSource>();
    private InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();

    private DataSourceInfoList.Info info;
    private String jndiName;
    private ServiceName dataSourceServiceName;
    private ServiceRegistry serviceRegistry;

    private org.jboss.logmanager.Logger logger;

}
