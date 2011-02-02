package org.torquebox.rack.deployers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;


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
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        
        rubyAppMetaData.setRoot(VFS.getChild("/foo"));
        rackAppMetaData.setEnvironmentVariables(environment);

		String deploymentName = createDeployment("test");
		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		
		unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
		unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

		processDeployments(true);

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey("SOME_VAR") );
        assertTrue( runtimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer );
	}
	
}
