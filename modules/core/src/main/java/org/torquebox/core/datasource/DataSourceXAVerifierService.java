package org.torquebox.core.datasource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.datasource.DataSourceInfoList.Info;

public class DataSourceXAVerifierService implements Service<DataSourceInfoList.Info> {

    public DataSourceXAVerifierService(Info info) {
        this.info = info;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if ( ! testSupportsXa() ) {
            this.info = info.DISABLED;
        }
        
        log.info( "Verifier completed" );
    }
    
    protected boolean testSupportsXa() {
        org.jboss.logmanager.Logger.getLogger( "com.arjuna.ats" ).setLevel( Level.OFF );
        try {
            DataSource dataSource = this.dataSourceInjector.getValue();
            TransactionManager tm = this.transactionManagerInjector.getValue();
            tm.begin();
            Transaction tx = tm.getTransaction();
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute( "SELECT 1;"); 
            statement.close();
            tx.enlistResource( new DummyXAResource() );
            tm.commit();
        } catch (Exception e) {
            log.warnf( "Error performing probe of XA datasource; XA will not be enabled: %s: %s", this.info.getName(), e.getMessage() );
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
    
    private ServiceRegistry registry;
    
    private org.jboss.logmanager.Logger logger;

}
