package org.torquebox.interp.deployers;

import java.net.MalformedURLException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

public class BaseRubyRuntimeDeployer extends AbstractDeployer {

    public BaseRubyRuntimeDeployer() {
        setInput(RubyApplicationMetaData.class);
        addInput(RubyRuntimeMetaData.class);
        setStage( DeploymentStages.PRE_DESCRIBE );
        setRelativeOrder( 10000 );
    }
    
    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(  RubyRuntimeMetaData.class );
        
        if ( runtimeMetaData != null ) {
            return;
        }
        
        log.debug("Deploying base ruby runtime: " + unit );
        
        RubyApplicationMetaData appMetaData = unit.getAttachment(  RubyApplicationMetaData.class  );
        
        runtimeMetaData = new RubyRuntimeMetaData();
        runtimeMetaData.setBaseDir( appMetaData.getRoot() );
        runtimeMetaData.setEnvironment(  appMetaData.getEnvironmentVariables()  );
        try {
            runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( appMetaData.getRoot().toURL() ) );
        } catch (MalformedURLException e) {
            throw new DeploymentException( e );
        }
        
        unit.addAttachment(  RubyRuntimeMetaData.class, runtimeMetaData );
    }

}
