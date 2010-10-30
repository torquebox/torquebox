package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.metadata.EnvironmentMetaData;


/**
 * <pre>
 * Stage: PRE_REAL
 *    In: EnvironmentMetaData, PoolMetaData
 *   Out: PoolMetaData
 * </pre>
 *
 * Ensures that pool metadata for messaging is available
 */
public class MessagingRuntimePoolDeployer extends AbstractDeployer {

    private String instanceFactoryName;

    public MessagingRuntimePoolDeployer() {
        setStage(DeploymentStages.PRE_REAL);
        addInput( EnvironmentMetaData.class );
        addInput(PoolMetaData.class);
        addOutput(PoolMetaData.class);
    }
    
    public void setInstanceFactoryName(String instanceFactoryName) {
        this.instanceFactoryName = instanceFactoryName;
    }
    
    public String getInstanceFactoryName() {
        return this.instanceFactoryName;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit.getAllMetaData(MessageProcessorMetaData.class).isEmpty()) {
            return;
        }

        Set<? extends PoolMetaData> allPools = unit.getAllMetaData(PoolMetaData.class);

        PoolMetaData pool = null;

        for (PoolMetaData each : allPools) {
            if (each.getName().equals("messaging")) {
                pool = each;
                break;
            }
        }

        if (pool == null) {
            EnvironmentMetaData envMetaData = unit.getAttachment(EnvironmentMetaData.class);
            boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
            pool = devMode ? new PoolMetaData("messaging", 1, 2) : new PoolMetaData("messaging");
            pool.setInstanceFactoryName( this.instanceFactoryName );
            log.info("Configured Ruby runtime pool for messaging: " + pool);
            AttachmentUtils.multipleAttach(unit, pool, "messaging");
        }
    }

}
