package org.torquebox.test.as;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.junit.Before;


public abstract class AbstractDeploymentProcessorTestCase {
    
    private List<DeploymentUnitProcessor> deployers = new ArrayList<DeploymentUnitProcessor>();
    
    @Before
    public void clearDeployers() {
        this.deployers.clear();
    }
    
    protected void addDeployer(DeploymentUnitProcessor deployer) {
        this.deployers.add(  deployer  );
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        for ( DeploymentUnitProcessor each : this.deployers ) {
            each.deploy( phaseContext );
        }
    }
    
    protected MockDeploymentPhaseContext createPhaseContext() throws Exception {
        return new MockDeploymentPhaseContext();
    }
    
    protected MockDeploymentPhaseContext createPhaseContext(String name, URL url) throws Exception {
        return new MockDeploymentPhaseContext( name, url );
    }

}
