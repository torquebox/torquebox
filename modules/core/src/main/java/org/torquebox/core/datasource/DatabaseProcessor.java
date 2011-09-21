package org.torquebox.core.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.logging.Logger;
import org.torquebox.core.datasource.db.Adapter;
import org.torquebox.core.datasource.db.H2Adapter;
import org.torquebox.core.datasource.db.PostgresAdapter;
import org.torquebox.core.datasource.db.MySQLAdapter;

public class DatabaseProcessor implements DeploymentUnitProcessor {

    public DatabaseProcessor() {
        addAdapter( new H2Adapter() );
        addAdapter( new PostgresAdapter() );
        addAdapter( new MySQLAdapter() );
    }

    protected void addAdapter(Adapter adapter) {
        this.adapters.put( adapter.getName(), adapter );
    }

    protected Adapter getAdapter(String adapterName) {
        return this.adapters.get( adapterName );
    }

    protected String getDriverClassName(String adapterName) {
        Adapter adapter = getAdapter( adapterName );

        if (adapter == null) {
            return null;
        }

        return adapter.getDriverClassName();
    }

    protected String getDataSourceClassName(String adapterName) {
        Adapter adapter = getAdapter( adapterName );

        if (adapter == null) {
            return null;
        }

        return adapter.getDriverClassName();
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        List<DatabaseMetaData> allMetaData = unit.getAttachmentList( DatabaseMetaData.ATTACHMENTS );

        Set<String> adapterNames = new HashSet<String>();

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
                processDriver( phaseContext, adapter );
            }

            try {
                processDataSource( phaseContext, each, adapter );
            } catch (ValidateException e) {
                log.warnf( "Unable to add data-source: %s", each.getConfigurationName() );
                throw new DeploymentUnitProcessingException( e );
            }
        }
    }

    protected void processDriver(DeploymentPhaseContext phaseContext, Adapter adapter) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        DriverMetaData driverMeta = new DriverMetaData( adapter.getName(), adapter.getDriverClassName() );
        unit.addToAttachmentList( DriverMetaData.ATTACHMENTS, driverMeta );
    }

    protected void processDataSource(DeploymentPhaseContext phaseContext, DatabaseMetaData dbMeta, Adapter adapter) throws ValidateException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        DataSourceMetaData dsMeta = new DataSourceMetaData( dbMeta.getConfigurationName(), adapter.getName(), adapter.getDataSourceClassName() );

        dsMeta.setJndiName( DataSourceServices.jndiName( unit, dsMeta.getName() ) );
        dsMeta.setProperties( adapter.getPropertiesFor( dbMeta ) );
        dsMeta.setSecurity( adapter.getSecurityFor( dbMeta ) );

        Integer pool = (Integer) dbMeta.getConfiguration().get( "pool" );
        if (pool != null) {
            dsMeta.setMaxPoolSize( pool );
        }
        unit.addToAttachmentList( DataSourceMetaData.ATTACHMENTS, dsMeta );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.db" );
    private Map<String, Adapter> adapters = new HashMap<String, Adapter>();

}
