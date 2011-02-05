package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class DeploymentNotifier extends AbstractDeployer {

    public DeploymentNotifier() {
        setStage( DeploymentStages.INSTALLED );
        setInput( RubyApplicationMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        log.info( "Fully deployed: " + unit.getName() );
    }

}
