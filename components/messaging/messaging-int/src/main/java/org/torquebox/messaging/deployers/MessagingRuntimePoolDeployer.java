package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, MessageProcessorMetaData
 *   Out: PoolMetaData
 * </pre>
 * 
 * Ensures that pool metadata for messaging is available
 */
public class MessagingRuntimePoolDeployer extends AbstractDeployer {

    private String instanceFactoryName;

    /**
     * I'd rather use setInput(MessageProcessorMetaData) and omit the
     * getAllMetaData short circuit in deploy(), but that requires attachers to
     * pass an ExpectedType, and I don't think we can assume that.
     */
    public MessagingRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( MessageProcessorMetaData.class );
        addInput( RubyApplicationMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    public void setInstanceFactoryName(String instanceFactoryName) {
        this.instanceFactoryName = instanceFactoryName;
    }

    public String getInstanceFactoryName() {
        return this.instanceFactoryName;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit.getAllMetaData( MessageProcessorMetaData.class ).isEmpty()) {
            return;
        }
        PoolMetaData pool = AttachmentUtils.getAttachment( unit, "messaging", PoolMetaData.class );
        if (pool == null) {
            RubyApplicationMetaData envMetaData = unit.getAttachment( RubyApplicationMetaData.class );
            boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
            pool = devMode ? new PoolMetaData( "messaging", 1, 2 ) : new PoolMetaData( "messaging" );
            pool.setInstanceFactoryName( this.instanceFactoryName );
            log.info( "Configured Ruby runtime pool for messaging: " + pool );
            AttachmentUtils.multipleAttach( unit, pool, "messaging" );
        }
    }

}
