package org.torquebox.jobs;

import java.net.URL;


public abstract class AbstractDeploymentProcessorTestCase {
    
    protected MockDeploymentPhaseContext createPhaseContext() throws Exception {
        return new MockDeploymentPhaseContext();
    }
    
    protected MockDeploymentPhaseContext createPhaseContext(String name, URL url) throws Exception {
        return new MockDeploymentPhaseContext( name, url );
    }

}
