package org.torquebox.core.datasource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.connector.registry.DriverRegistry;
import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.connector.subsystems.datasources.XaDataSourceService;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.naming.service.NamingService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.common.CommonXaPoolImpl;
import org.jboss.jca.common.metadata.ds.XADataSourceImpl;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.datasource.DataSourceInfoList.Info;
import org.torquebox.core.datasource.db.Adapter;
import org.torquebox.core.datasource.db.H2Adapter;
import org.torquebox.core.datasource.db.MySQLAdapter;
import org.torquebox.core.datasource.db.PostgresAdapter;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class DatabaseProcessor implements DeploymentUnitProcessor {

    public DatabaseProcessor() {
        addAdapter( new H2Adapter() );
        addAdapter( new PostgresAdapter() );
        addAdapter( new MySQLAdapter() );
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

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        DriverManager.setLogWriter( new PrintWriter( System.err ) );

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            return;
        }

        String applicationDir;
        try {
            applicationDir = rubyAppMetaData.getRoot().getPhysicalFile().getAbsolutePath();
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        List<DatabaseMetaData> allMetaData = unit.getAttachmentList( DatabaseMetaData.ATTACHMENTS );

        Set<String> adapterNames = new HashSet<String>();

        DataSourceInfoList infoList = new DataSourceInfoList();

        for (DatabaseMetaData each : allMetaData) {

            if (each.getConfiguration().containsKey( "jndi" )) {
                continue;
            }

            String adapterName = (String) each.getConfiguration().get( "adapter" );

            Adapter adapter = getAdapter( adapterName );

            if (adapter == null) {
                log.errorf( "Unknown adapter type: %s", adapterName );
                continue;
            }

            if (adapterNames.add( adapterName )) {
                processDriver( phaseContext, adapter, applicationDir );
            }

            try {
                Info info = processDataSource( phaseContext, each, adapter );
                infoList.addConfiguration( info );
            } catch (ValidateException e) {
                log.warnf( "Unable to add data-source: %s", each.getConfigurationName() );
                throw new DeploymentUnitProcessingException( e );
            }
        }

        if (!adapterNames.isEmpty()) {
            installJDBCDriverLoadingRuntime( phaseContext );
        }

        processDataSourceInfos( phaseContext, infoList );
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

    private void processDataSourceInfos(DeploymentPhaseContext phaseContext, DataSourceInfoList infoList) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        Service<DataSourceInfoList> service = new ValueService<DataSourceInfoList>( new ImmediateValue<DataSourceInfoList>( infoList ) );
        ServiceBuilder<DataSourceInfoList> builder = phaseContext.getServiceTarget().addService( DataSourceServices.dataSourceInfoName( unit ), service );
        
        for ( DataSourceInfoList.Info each : infoList.getConfigurations() ) {
            builder.addDependencies( each.getServiceName() );
        }
        
        builder.install();
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

    protected DataSourceInfoList.Info processDataSource(DeploymentPhaseContext phaseContext, DatabaseMetaData dbMeta, Adapter adapter) throws ValidateException,
            DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        final String jndiName = DataSourceServices.jndiName( unit, dbMeta.getConfigurationName() );
        final ServiceName dataSourceServiceName = DataSourceServices.datasourceName( unit, dbMeta.getConfigurationName() );
        
        try {
            final XaDataSource config = createConfig( unit, dbMeta, adapter );
            XaDataSourceService service = new XaDataSourceService( jndiName );

            service.getDataSourceConfigInjector().inject( config );
            phaseContext.getServiceTarget().addService( dataSourceServiceName, service )
                    .addDependency( ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE, DriverRegistry.class, service.getDriverRegistryInjector() )
                    .addDependency( DataSourceServices.driverName( unit, adapter.getId() ), Driver.class, service.getDriverInjector() )
                    .addDependency( ConnectorServices.MANAGEMENT_REPOSISTORY_SERVICE, ManagementRepository.class, service.getmanagementRepositoryInjector() )
                    .addDependency( ConnectorServices.TRANSACTION_INTEGRATION_SERVICE, TransactionIntegration.class, service.getTransactionIntegrationInjector() )
                    .addDependency( NamingService.SERVICE_NAME )
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

        } catch (ValidateException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        String adapterName = (String) dbMeta.getConfiguration().get( "adapter" );
        return new DataSourceInfoList.Info( dbMeta.getConfigurationName(), jndiName, adapterName, dataSourceServiceName );
    }

    protected XaDataSource createConfig(DeploymentUnit unit, DatabaseMetaData dbMeta, Adapter adapter) throws ValidateException {
        TransactionIsolation transactionIsolation = null;
        TimeOut timeOut = null;
        Statement statement = null;
        Validation validation = null;
        String urlDelimiter = null;
        String urlSelectorStrategyClassName = null;
        boolean useJavaContext = false;
        String poolName = unit.getName() + "." + dbMeta.getConfigurationName();
        boolean enabled = true;
        boolean spy = false;
        boolean useCcm = false;
        String newConnectionSql = null;
        CommonXaPool xaPool = createPool( dbMeta );
        Recovery recovery = null;

        return new XADataSourceImpl(
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

    protected CommonXaPool createPool(DatabaseMetaData dsMeta) throws ValidateException {
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

        return new CommonXaPoolImpl( minPoolSize,
                maxPoolSize,
                prefill,
                useStrictMin,
                flushStrategy,
                isSameRmOverride,
                interleaving,
                padXid,
                wrapXaDataSource,
                noTxSeparatePool );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.db" );
    private Map<String, Adapter> adapters = new HashMap<String, Adapter>();

}
