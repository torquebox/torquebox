package org.torquebox.base.deployers;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class RubyApplicationClassLoaderDeployer extends AbstractDeployer {
    
    public RubyApplicationClassLoaderDeployer() {
        setStage( DeploymentStages.POST_PARSE );
        setInput( RubyApplicationMetaData.class );
        addInput( ClassLoadingMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        
        log.info(  "Set up classloader for: " + unit  );
        ClassLoadingMetaData clMetaData = unit.getAttachment( ClassLoadingMetaData.class );
        
        if ( clMetaData != null ) {
            log.info( "ClassLoader already configured: " + clMetaData );
            // someone already did something?
            return;
        }
        
        clMetaData = new ClassLoadingMetaData();
        clMetaData.setDomain( unit.getName() );
        log.info( "Configuring as: " + clMetaData );
        unit.addAttachment( ClassLoadingMetaData.class, clMetaData );
    }

}
