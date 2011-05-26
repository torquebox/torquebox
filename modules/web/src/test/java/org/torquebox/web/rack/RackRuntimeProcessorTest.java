package org.torquebox.web.rack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jboss.vfs.VFS;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.runtime.RubyRuntimeMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class RackRuntimeProcessorTest extends AbstractDeploymentProcessorTestCase {
	
    private Map<String, String> environment = new HashMap<String, String>();
	
	@Before
	public void setUpDeployer() {
		appendDeployer( new RackRuntimeProcessor() );
	}
	
    @Test
    public void testHappy() throws Exception {
        environment.put( "SOME_VAR", "gassy" );
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "app_name");
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );
        rubyAppMetaData.setEnvironmentVariables( environment );

        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        unit.putAttachment( RackApplicationMetaData.ATTACHMENT_KEY, rackAppMetaData );

        deploy( phaseContext );
        
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey( "SOME_VAR" ) );
        assertTrue( runtimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RACK, runtimeMetaData.getRuntimeType() );
    }

    @Test
    public void testWithExistingRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "app_name");
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData originalRuntimeMD = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, originalRuntimeMD );
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        unit.putAttachment( RackApplicationMetaData.ATTACHMENT_KEY, rackAppMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( originalRuntimeMD, runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RACK, runtimeMetaData.getRuntimeType() );
    }

    @Test
    public void testWithExistingTypedRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "app_name" );
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData originalRuntimeMD = new RubyRuntimeMetaData();
        originalRuntimeMD.setRuntimeType( RubyRuntimeMetaData.RuntimeType.BARE );

        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, originalRuntimeMD );
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        unit.putAttachment( RackApplicationMetaData.ATTACHMENT_KEY, rackAppMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( originalRuntimeMD, runtimeMetaData );
        assertNull( runtimeMetaData.getBaseDir() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }

}
