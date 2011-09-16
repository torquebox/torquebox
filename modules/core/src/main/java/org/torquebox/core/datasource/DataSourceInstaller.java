package org.torquebox.core.datasource;

import java.sql.Driver;
import java.util.List;

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
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

public class DataSourceInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<DataSourceMetaData> allMetaData = unit.getAttachmentList( DataSourceMetaData.ATTACHMENTS );
        for (DataSourceMetaData each : allMetaData) {
            process( phaseContext, each );
        }
        
        deployDataSourceInfo( phaseContext, allMetaData );
    }

    protected void deployDataSourceInfo(DeploymentPhaseContext phaseContext, List<DataSourceMetaData> allMetaData) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ServiceName name = DataSourceServices.dataSourceInfoName( unit );
        ValueService<DataSourceInfo> service = new ValueService<DataSourceInfo>( new ImmediateValue<DataSourceInfo>( new DataSourceInfo( allMetaData )  ) ); 
        
        ServiceBuilder<DataSourceInfo> builder = phaseContext.getServiceTarget().addService( name, service )
            .setInitialMode( Mode.ON_DEMAND );
        
        for ( DataSourceMetaData each : allMetaData ) {
            ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor( each.getJndiName() );
            builder.addDependency( bindInfo.getBinderServiceName() );
        }
        
        builder.install();
    }

    protected void process(DeploymentPhaseContext phaseContext, final DataSourceMetaData dsMeta) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        try {
            final XaDataSource config = createConfig( unit, dsMeta );
            XaDataSourceService service = new XaDataSourceService( dsMeta.getJndiName() );

            service.getDataSourceConfigInjector().inject( config );
            ServiceName dataSourceServiceName = DataSourceServices.datasourceName( unit, dsMeta.getName() );
            phaseContext.getServiceTarget().addService( dataSourceServiceName, service )
                    .addDependency( ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE, DriverRegistry.class, service.getDriverRegistryInjector() )
                    .addDependency( DataSourceServices.driverName( unit, dsMeta.getDriverName() ), Driver.class, service.getDriverInjector() )
                    .addDependency( ConnectorServices.MANAGEMENT_REPOSISTORY_SERVICE, ManagementRepository.class, service.getmanagementRepositoryInjector() )
                    .addDependency( ConnectorServices.TRANSACTION_INTEGRATION_SERVICE, TransactionIntegration.class, service.getTransactionIntegrationInjector() )
                    .addDependency( NamingService.SERVICE_NAME )
                    .install();

            final DataSourceReferenceFactoryService referenceFactoryService = new DataSourceReferenceFactoryService();
            final ServiceName referenceFactoryServiceName = DataSourceReferenceFactoryService.SERVICE_NAME_BASE.append( dsMeta.getJndiName() );
            final ServiceBuilder<?> referenceBuilder = phaseContext.getServiceTarget().addService( referenceFactoryServiceName,
                    referenceFactoryService ).addDependency( dataSourceServiceName, DataSource.class,
                    referenceFactoryService.getDataSourceInjector() );

            referenceBuilder.setInitialMode( Mode.ACTIVE );
            referenceBuilder.install();

            final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor( dsMeta.getJndiName() );
            final BinderService binderService = new BinderService( bindInfo.getBindName() );
            final ServiceBuilder<?> binderBuilder = phaseContext.getServiceTarget()
                    .addService( bindInfo.getBinderServiceName(), binderService )
                    .addDependency( referenceFactoryServiceName, ManagedReferenceFactory.class, binderService.getManagedObjectInjector() )
                    .addDependency( bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector() )
                    .addListener( new AbstractServiceListener<Object>() {
                        public void transition(final ServiceController<? extends Object> controller, final ServiceController.Transition transition) {
                            switch (transition) {
                            case STARTING_to_UP: {
                                log.infof( "Bound data source [%s]", dsMeta.getJndiName() );
                                break;
                            }
                            case START_REQUESTED_to_DOWN: {
                                log.infof( "Unbound data source [%s]", dsMeta.getJndiName() );
                                break;
                            }
                            case REMOVING_to_REMOVED: {
                                log.debugf( "Removed JDBC Data-source [%s]", dsMeta.getJndiName() );
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
    }

    protected XaDataSource createConfig(DeploymentUnit unit, DataSourceMetaData dsMeta) throws ValidateException {
        TransactionIsolation transactionIsolation = null;
        TimeOut timeOut = null;
        Statement statement = null;
        Validation validation = null;
        String urlDelimiter = null;
        String urlSelectorStrategyClassName = null;
        boolean useJavaContext = false;
        String poolName = unit.getName() + "." + dsMeta.getName();
        boolean enabled = true;
        boolean spy = false;
        boolean useCcm = false;
        String newConnectionSql = null;
        CommonXaPool xaPool = createPool( dsMeta );
        Recovery recovery = null;

        return new XADataSourceImpl(
                transactionIsolation,
                timeOut,
                dsMeta.getSecurity(),
                statement,
                validation,
                urlDelimiter,
                urlSelectorStrategyClassName,
                useJavaContext,
                poolName,
                enabled,
                dsMeta.getJndiName(),
                spy,
                useCcm,
                dsMeta.getProperties(),
                dsMeta.getDataSourceClassName(),
                dsMeta.getDriverName(),
                newConnectionSql,
                xaPool,
                recovery );
    }
    
    protected CommonXaPool createPool(DataSourceMetaData dsMeta) throws ValidateException {
        Integer minPoolSize = 0;
        Integer maxPoolSize = dsMeta.getMaxPoolSize();
        Boolean prefill = false;
        Boolean useStrictMin = false;
        FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
        Boolean isSameRmOverride = false;
        ;
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

}
