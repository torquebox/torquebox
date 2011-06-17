/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.test.as;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
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
    
    protected void undeploy(DeploymentUnit unit) {
        for ( DeploymentUnitProcessor each : this.deployers ) {
            each.undeploy( unit );
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
