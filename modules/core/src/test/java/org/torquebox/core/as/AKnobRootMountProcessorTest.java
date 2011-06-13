package org.torquebox.core.as;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;

public class AKnobRootMountProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new AKnobRootMountProcessor() );
    }
    
    @Test
    public void testUnmountsOnUndeploy() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        MountHandle mountHandle = mock( MountHandle.class );
        VirtualFile root = VFS.getChild( "." );
        ResourceRoot resourceRoot = new ResourceRoot( root, mountHandle );
        unit.putAttachment( Attachments.DEPLOYMENT_ROOT, resourceRoot );
        
        undeploy( unit );
        
        verify( mountHandle ).close();
    }

}
