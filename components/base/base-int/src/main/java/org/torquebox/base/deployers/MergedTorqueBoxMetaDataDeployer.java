package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.TorqueBoxMetaData;

public class MergedTorqueBoxMetaDataDeployer extends AbstractDeployer {

    public MergedTorqueBoxMetaDataDeployer() {
        addInput(TorqueBoxMetaData.class);
        addInput(TorqueBoxMetaData.EXTERNAL);
        addOutput(TorqueBoxMetaData.class);
        setAllInputs( true );
        setStage(DeploymentStages.PARSE);
        setRelativeOrder( -1000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        TorqueBoxMetaData externalMetaData = unit.getAttachment( TorqueBoxMetaData.EXTERNAL, TorqueBoxMetaData.class );
        log.debug( "External: " + externalMetaData );
        
        if ( externalMetaData == null ) {
            return;
        }
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.class );
        log.debug( "Internal: " + metaData );
        
        if ( metaData == null ) {
            unit.addAttachment( TorqueBoxMetaData.class, externalMetaData );
            return;
        }
        
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto( metaData );
        
        log.debug( "Merged: " + mergedMetaData );
        
        unit.addAttachment( TorqueBoxMetaData.class, mergedMetaData );
    }
}
