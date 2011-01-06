package org.torquebox.rack.deployers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import org.jboss.vfs.VFS;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;


public class RackRuntimeDeployerTest extends AbstractDeployerTestCase {
	
	private RackRuntimeDeployer deployer;
    private Map<String,String> environment = new HashMap<String,String>();

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RackRuntimeDeployer();
		addDeployer( this.deployer );
	}
	
	@Test
	public void testHappy() throws Exception {
        environment.put("SOME_VAR", "gassy");
        RackApplicationMetaData metadata = new RackApplicationMetaData();
        metadata.setRackRoot(VFS.getChild("/foo"));
        metadata.setEnvironmentVariables(environment);

		String deploymentName = createDeployment("test");
		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment( RackApplicationMetaData.class, metadata );

		processDeployments(true);

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey("SOME_VAR") );
        assertTrue( runtimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer );
	}
	
}
