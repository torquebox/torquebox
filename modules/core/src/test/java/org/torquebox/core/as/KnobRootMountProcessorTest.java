package org.torquebox.core.as;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Closeable;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Test;
import org.projectodd.polyglot.test.as.AbstractDeploymentProcessorTestCase;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;

public class KnobRootMountProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Test
    public void testUnmountsOnUndeploy() throws Exception {
        KnobRootMountProcessor deployer = spy( new KnobRootMountProcessor() );
        appendDeployer( deployer );
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        Closeable closeable = mock( Closeable.class );
        when( deployer.getKnobCloseable() ).thenReturn( closeable );
        
        undeploy( unit );
        
        verify( closeable ).close();
    }

}
