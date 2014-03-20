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

package org.torquebox.core.datasource.processors;

import java.sql.Driver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.jboss.as.connector.services.driver.registry.DriverRegistry;
import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.connector.subsystems.datasources.ModifiableXaDataSource;
import org.jboss.as.connector.subsystems.datasources.XaDataSourceService;
import org.jboss.as.connector.util.ConnectorServices;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.naming.service.NamingService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.txn.service.TxnServices;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.v11.DsXaPool;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.v11.DsXaPoolImpl;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jruby.Ruby;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.datasource.DataSourceInfoList;
import org.torquebox.core.datasource.DataSourceInfoList.Info;
import org.torquebox.core.datasource.DataSourceInfoListService;
import org.torquebox.core.datasource.DataSourceServices;
import org.torquebox.core.datasource.DataSourceXAVerifierService;
import org.torquebox.core.datasource.DatabaseMetaData;
import org.torquebox.core.datasource.DriverService;
import org.torquebox.core.datasource.JDBCDriverLoadingRuntimeService;
import org.torquebox.core.datasource.db.Adapter;
import org.torquebox.core.datasource.db.H2Adapter;
import org.torquebox.core.datasource.db.MySQLAdapter;
import org.torquebox.core.datasource.db.OracleAdapter;
import org.torquebox.core.datasource.db.PostgresAdapter;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class DatabaseProcessor implements DeploymentUnitProcessor {

    public DatabaseProcessor() {
        addAdapter( new H2Adapter() );
        addAdapter( new PostgresAdapter() );
        addAdapter( new MySQLAdapter() );
        addAdapter( new OracleAdapter() );
        // DriverManager.setLogWriter( new PrintWriter( System.err ) );
    }

    protected void addAdapter(Adapter adapter) {
        String[] names = adapter.getNames();
        for (String name : names) {
            this.adapters.put( name, adapter );
        }
    }

    protected Adapter getAdapter(String adapterName) {
        return this.adapters.get( adapterName );
    }

    protected boolean isCurrentEnvironmentDatabase(String currentEnv, String configName) {
        return currentEnv.equals( configName );
    }

    protected boolean isXAExplicitlyEnabled(DatabaseMetaData metaData) {
        Object xaEntry = metaData.getConfiguration().get( "xa" );
        return xaEntry != null && xaEntry == Boolean.TRUE;
    }

    protected boolean isXAExplicitlyDisabled(DatabaseMetaData metaData) {
        Object xaEntry = metaData.getConfiguration().get( "xa" );
        return xaEntry != null && xaEntry == Boolean.FALSE;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            return;
        }

        String currentEnv = rubyAppMetaData.getEnvironmentName();
        String applicationDir = rubyAppMetaData.getRoot().getAbsolutePath();

        List<DatabaseMetaData> allMetaData = unit.getAttachmentList( DatabaseMetaData.ATTACHMENTS );

        Set<String> adapterIds = new HashSet<String>();

        DataSourceInfoListService dsInfoService = new DataSourceInfoListService( org.jboss.logmanager.Logger.getLogger( "com.arjuna.ats" ).getLevel() );
        ServiceBuilder<DataSourceInfoList> dsInfoBuilder = phaseContext.getServiceTarget().addService( DataSourceServices.dataSourceInfoName( unit ), dsInfoService );

        for (DatabaseMetaData each : allMetaData) {

            if (!isXAExplicitlyEnabled( each )) {
                continue;
            }

            if (each.getConfiguration().containsKey( "jndi" )) {
                continue;
            }

            String adapterName = (String) each.getConfiguration().get( "adapter" );

            Adapter adapter = getAdapter( adapterName );

            if (adapter == null) {
                log.warnf( "Not enabling XA for unknown adapter type: %s", adapterName );
                continue;
            }

            if (adapterIds.add( adapter.getId() )) {
                processDriver( phaseContext, adapter, applicationDir );
            }

            try {
                ServiceName dsVerifierServiceName = processDataSource( phaseContext, each, adapter );
                dsInfoBuilder.addDependency( dsVerifierServiceName, Info.class, dsInfoService.getInfoInjector() );
            } catch (ValidateException e) {
                log.warnf( "Unable to add data-source: %s", each.getConfigurationName() );
                throw new DeploymentUnitProcessingException( e );
            }
        }

        dsInfoBuilder.install();

        if (!adapterIds.isEmpty()) {
            installJDBCDriverLoadingRuntime( phaseContext );
        }

    }

    private void installJDBCDriverLoadingRuntime(DeploymentPhaseContext phaseContext) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        JDBCDriverLoadingRuntimeService service = new JDBCDriverLoadingRuntimeService();
        ServiceName name = DataSourceServices.jdbcDriverLoadingRuntimeName( unit );
        phaseContext.getServiceTarget().addService( name, service )
                .addDependency( CoreServices.runtimeFactoryName( unit ).append( "lightweight" ), RubyRuntimeFactory.class, service.getRuntimeFactoryInjector() )
                .setInitialMode( Mode.ON_DEMAND )
                .install();
    }

    // ------------------------------------------------------------------------
    // Drivers
    // ------------------------------------------------------------------------

    protected void processDriver(DeploymentPhaseContext phaseContext, Adapter adapter, String applicationDir) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        DriverService driverService = new DriverService( applicationDir, adapter );

        ServiceName name = DataSourceServices.driverName( unit, adapter.getId() );

        phaseContext.getServiceTarget().addService( name, driverService )
                .addDependency( ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE, DriverRegistry.class, driverService.getDriverRegistryInjector() )
                .addDependency( DataSourceServices.jdbcDriverLoadingRuntimeName( unit ), Ruby.class, driverService.getRuntimeInjector() )
                .setInitialMode( ServiceController.Mode.ACTIVE ).install();

    }

    // ------------------------------------------------------------------------
    // DataSources
    // ------------------------------------------------------------------------

    protected ServiceName processDataSource(DeploymentPhaseContext phaseContext, DatabaseMetaData dbMeta, Adapter adapter) throws ValidateException,
            DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        final String jndiName = DataSourceServices.jndiName( unit, dbMeta.getConfigurationName() );
        final ServiceName dataSourceServiceName = DataSourceServices.datasourceName( unit, dbMeta.getConfigurationName() );

        try {
            final ModifiableXaDataSource config = createConfig( unit, dbMeta, adapter );
            XaDataSourceService service = new XaDataSourceService( jndiName );

            service.getDataSourceConfigInjector().inject( config );
            phaseContext.getServiceTarget().addService( dataSourceServiceName, service )
                    .addDependency( ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE, DriverRegistry.class, service.getDriverRegistryInjector() )
                    .addDependency( DataSourceServices.driverName( unit, adapter.getId() ), Driver.class, service.getDriverInjector() )
                    .addDependency( ConnectorServices.MANAGEMENT_REPOSITORY_SERVICE, ManagementRepository.class, service.getManagementRepositoryInjector() )
                    .addDependency( ConnectorServices.TRANSACTION_INTEGRATION_SERVICE, TransactionIntegration.class, service.getTransactionIntegrationInjector() )
                    .addDependency( NamingService.SERVICE_NAME )
                    .addDependency(ConnectorServices.CCM_SERVICE, CachedConnectionManager.class, service.getCcmInjector())
                    .install();

            final DataSourceReferenceFactoryService referenceFactoryService = new DataSourceReferenceFactoryService();
            final ServiceName referenceFactoryServiceName = DataSourceReferenceFactoryService.SERVICE_NAME_BASE.append( jndiName );
            final ServiceBuilder<?> referenceBuilder = phaseContext.getServiceTarget().addService( referenceFactoryServiceName,
                    referenceFactoryService ).addDependency( dataSourceServiceName, DataSource.class,
                    referenceFactoryService.getDataSourceInjector() );

            referenceBuilder.setInitialMode( Mode.ACTIVE );
            referenceBuilder.install();

            final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor( jndiName );
            final BinderService binderService = new BinderService( bindInfo.getBindName() );
            final ServiceBuilder<?> binderBuilder = phaseContext.getServiceTarget()
                    .addService( bindInfo.getBinderServiceName(), binderService )
                    .addDependency( referenceFactoryServiceName, ManagedReferenceFactory.class, binderService.getManagedObjectInjector() )
                    .addDependency( bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector() )
                    .addListener( new AbstractServiceListener<Object>() {
                        public void transition(final ServiceController<? extends Object> controller, final ServiceController.Transition transition) {
                            switch (transition) {
                            case STARTING_to_UP: {
                                log.infof( "Bound data source [%s]", jndiName );
                                break;
                            }
                            case START_REQUESTED_to_DOWN: {
                                log.infof( "Unbound data source [%s]", jndiName );
                                break;
                            }
                            case REMOVING_to_REMOVED: {
                                log.debugf( "Removed JDBC Data-source [%s]", jndiName );
                                break;
                            }
                            }
                        }
                    } );

            binderBuilder.setInitialMode( Mode.ACTIVE );
            binderBuilder.install();

            String adapterName = (String) dbMeta.getConfiguration().get( "adapter" );
            Info dsInfo = new DataSourceInfoList.Info( dbMeta.getConfigurationName(), jndiName, adapterName, dataSourceServiceName, adapter );

            DataSourceXAVerifierService verifierService = new DataSourceXAVerifierService( dsInfo, phaseContext.getServiceRegistry(), jndiName );
            ServiceName verifierServiceName = dataSourceServiceName.append( "xa-verifier" );
            phaseContext.getServiceTarget().addService( verifierServiceName, verifierService )
                    .addDependency( DependencyType.OPTIONAL, dataSourceServiceName, DataSource.class, verifierService.getDataSourceInjector() )
                    .addDependency( TxnServices.JBOSS_TXN_TRANSACTION_MANAGER, TransactionManager.class, verifierService.getTransactionManagerInjector() )
                    .install();

            return verifierServiceName;

        } catch (ValidateException e) {
            throw new DeploymentUnitProcessingException( e );
        }

    }

    protected ModifiableXaDataSource createConfig(DeploymentUnit unit, DatabaseMetaData dbMeta, Adapter adapter) throws ValidateException {
        TransactionIsolation transactionIsolation = null;
        TimeOut timeOut = null;
        Statement statement = null;
        Validation validation = adapter.getValidationFor( dbMeta );
        String urlDelimiter = null;
        String urlSelectorStrategyClassName = null;
        boolean useJavaContext = false;
        String poolName = unit.getName() + "." + dbMeta.getConfigurationName();
        boolean enabled = true;
        boolean spy = false;
        boolean useCcm = false;
        String newConnectionSql = null;
        DsXaPool xaPool = createPool( dbMeta );
        Recovery recovery = null;

        return new ModifiableXaDataSource(
                transactionIsolation,
                timeOut,
                adapter.getSecurityFor( dbMeta ),
                statement,
                validation,
                urlDelimiter,
                urlSelectorStrategyClassName,
                useJavaContext,
                poolName,
                enabled,
                DataSourceServices.jndiName( unit, dbMeta.getConfigurationName() ),
                spy,
                useCcm,
                adapter.getPropertiesFor( dbMeta ),
                adapter.getDataSourceClassName(),
                adapter.getId(),
                newConnectionSql,
                xaPool,
                recovery );
    }

    protected DsXaPool createPool(DatabaseMetaData dsMeta) throws ValidateException {
        Integer minPoolSize = 0;
        Integer maxPoolSize = (Integer) dsMeta.getConfiguration().get( "pool" );
        Boolean prefill = false;
        Boolean useStrictMin = false;
        FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
        Boolean isSameRmOverride = false;
        Boolean interleaving = false;
        Boolean padXid = false;
        Boolean wrapXaDataSource = false;
        Boolean noTxSeparatePool = false;
        Boolean allowMultipleUsers = true;

        return new DsXaPoolImpl( minPoolSize,
                maxPoolSize,
                prefill,
                useStrictMin,
                flushStrategy,
                isSameRmOverride,
                interleaving,
                padXid,
                wrapXaDataSource,
                noTxSeparatePool,
                allowMultipleUsers );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("serial")
    private static final Set<String> TYPICAL_ENVIRONMENTS = new HashSet<String>() {
        {
            add( "development" );
            add( "production" );
            add( "test" );
        }
    };

    private static final Logger log = Logger.getLogger( "org.torquebox.db" );
    private Map<String, Adapter> adapters = new HashMap<String, Adapter>();

}
