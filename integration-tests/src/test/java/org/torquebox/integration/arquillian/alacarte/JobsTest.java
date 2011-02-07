package org.torquebox.integration.arquillian.alacarte;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class JobsTest extends AbstractIntegrationTestCase {
    
    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "alacarte/jobs-knob.yml" );
    }
    
    @Test
    public void testDeployment() throws InterruptedException {
        Thread.sleep( 5000 );
    }

}
