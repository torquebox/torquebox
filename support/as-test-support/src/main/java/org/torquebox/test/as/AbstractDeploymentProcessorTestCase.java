package org.torquebox.test.as;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.junit.After;
import org.junit.Before;
import org.torquebox.test.AbstractTorqueBoxTestCase;


public abstract class AbstractDeploymentProcessorTestCase extends AbstractTorqueBoxTestCase {
    
    private List<DeploymentUnitProcessor> deployers = new ArrayList<DeploymentUnitProcessor>();
    
    private List<MockDeploymentPhaseContext> contexts = new ArrayList<MockDeploymentPhaseContext>();
    
    @Before
    public void clearDeployers() {
        this.deployers.clear();
    }
    
    @After
    public void closeAllContexts() {
        for ( MockDeploymentPhaseContext each : this.contexts ) {
            each.close();
        }
        
        this.contexts.clear();
    }
    
    protected void prependDeployer(DeploymentUnitProcessor deployer) {
        this.deployers.add( 0, deployer );
    }
    
    protected void appendDeployer(DeploymentUnitProcessor deployer) {
        this.deployers.add(  deployer  );
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        for ( DeploymentUnitProcessor each : this.deployers ) {
            each.deploy( phaseContext );
        }
    }
    
    protected MockDeploymentPhaseContext createPhaseContext() throws Exception {
        MockDeploymentPhaseContext context = new MockDeploymentPhaseContext();
        this.contexts.add(  context  );
        return context;
    }
    
    protected MockDeploymentPhaseContext createPhaseContext(String name, URL url) throws Exception {
        MockDeploymentPhaseContext context = new MockDeploymentPhaseContext( name, url );
        this.contexts.add(  context  );
        return context;
    }

}
