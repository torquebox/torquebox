package org.torquebox.interp.deployers;

import static org.junit.Assert.*;

import java.net.URL;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubyYamlParsingDeployerTest extends AbstractDeployerTestCase {
	private RubyYamlParsingDeployer deployer;
	
	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RubyYamlParsingDeployer();
		addDeployer( this.deployer );
	}
	
	@Test
	public void testNoRuntimeMetaData() throws Exception {
        URL rubyYml = getClass().getResource("ruby-1.8.yml");
        
        String deploymentName = addDeployment(rubyYml, "ruby.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        assertTrue( unit.getAllMetaData( RubyRuntimeMetaData.class ).isEmpty() );
	}
	
	@Test
	public void testWithRuntimeMetaData18() throws Exception {
        URL rubyYml = getClass().getResource("ruby-1.8.yml");
        
        String deploymentName = addDeployment(rubyYml, "ruby.yml");
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.addAttachment( RubyRuntimeMetaData.class, runtimeMetaData );
        
        processDeployments(true);
        
        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.class );
        
        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.Version.V1_8, runtimeMetaData.getVersion() );
	}
	
	@Test
	public void testWithRuntimeMetaData19() throws Exception {
        URL rubyYml = getClass().getResource("ruby-1.9.yml");
        
        String deploymentName = addDeployment(rubyYml, "ruby.yml");
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.addAttachment( RubyRuntimeMetaData.class, runtimeMetaData );
        
        processDeployments(true);
        
        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.class );
        
        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.Version.V1_9, runtimeMetaData.getVersion() );
	}
	

}
