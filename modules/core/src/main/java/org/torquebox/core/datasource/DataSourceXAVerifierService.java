/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public DataSourceXAVerifierService(Info info, ServiceRegistry serviceRegistry, String jndiName) {
        this.info = info;
        this.serviceRegistry = serviceRegistry;
        this.jndiName = jndiName;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (!testSupportsXa()) {
            this.info = Info.DISABLED;
            removeBinder();
            removeReferenceFactory();
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
            if ( getAdapterId().equals( "postgresql" ) ) {
                if ( postgresqlMaxPreparedTransactions( dataSource ) == 0 ) {
                    log.warnf( "PostgreSQL max_prepared_transactions set to 0; XA will not be enabled: %s", this.info.getName() );
                    return false;
                }
            }
            tm.begin();
            Transaction tx = tm.getTransaction();
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            String sql = "SELECT 1;";
            if (getAdapterId().equals( "oracle" )) {
                sql = "SELECT 1 FROM DUAL";
            }
            statement.execute( sql );
            statement.close();
            tx.enlistResource( new DummyXAResource() );
            tm.commit();
        } catch (Exception e) {
            log.warnf( "Error performing probe of XA datasource; XA will not be enabled: %s", this.info.getName() );
            return false;
        }

        return true;
    }

    protected int postgresqlMaxPreparedTransactions(DataSource dataSource) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            ResultSet result = statement.executeQuery( "show max_prepared_transactions;" );
            result.next();
            int max = result.getInt( 1 );
            return max;
        } catch (SQLException e) {
            log.warnf( "Error determining PostgreSQL max_prepared_transactions: %s", this.info.getName() );
            return 0;
        } finally {
            try {
                if (statement != null) { statement.close(); }
                if (connection != null) { connection.close(); }
            } catch (SQLException ignored) { }
        }
    }

    protected String getAdapterId() {
        return this.info.getAdapter().getId();
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
    private ServiceRegistry serviceRegistry;

    private org.jboss.logmanager.Logger logger;

}
