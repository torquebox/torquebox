package org.torquebox.core.as;

import java.net.MalformedURLException;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.runtime.RubyLoadPathMetaData;
import org.torquebox.core.runtime.RubyRuntimeMetaData;

public class BaseRubyRuntimeDeployer implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        
        if ( rubyAppMetaData == null ) {
            return;
        }
        
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(  RubyRuntimeMetaData.ATTACHMENT_KEY );
        
        if ( runtimeMetaData != null && runtimeMetaData.getRuntimeType() != null ) {
            return;
        }

        if (runtimeMetaData == null) {
            runtimeMetaData = new RubyRuntimeMetaData();
            unit.putAttachment(  RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );
        }

        runtimeMetaData.setBaseDir( rubyAppMetaData.getRoot() );
        runtimeMetaData.setEnvironment( rubyAppMetaData.getEnvironmentVariables() );
        runtimeMetaData.setRuntimeType( RubyRuntimeMetaData.RuntimeType.RACK );

        try {
            runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( rubyAppMetaData.getRoot().toURL() ) );
        } catch (MalformedURLException e) {
            throw new DeploymentUnitProcessingException( e );
        }

    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
