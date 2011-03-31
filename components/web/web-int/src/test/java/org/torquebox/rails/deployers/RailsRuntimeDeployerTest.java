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

package org.torquebox.rails.deployers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;

import org.junit.Before;
import org.junit.Test;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.core.RailsRuntimeInitializer;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RailsRuntimeDeployerTest extends AbstractDeployerTestCase {

    private RailsRuntimeDeployer deployer;
    private Map<String, String> environment = new HashMap<String, String>();

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new RailsRuntimeDeployer();
        addDeployer( this.deployer );
    }

    @Test
    public void testHappy() throws Exception {
        environment.put( "SOME_VAR", "gassy" );
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RailsApplicationMetaData railsAppMetaData = new RailsApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );
        rubyAppMetaData.setEnvironmentVariables( environment );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey( "SOME_VAR" ) );
        assertTrue( runtimeMetaData.getRuntimeInitializer() instanceof RailsRuntimeInitializer );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RAILS, runtimeMetaData.getRuntimeType() );
    }

    @Test
    public void testWithExistingRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RailsApplicationMetaData railsAppMetaData = new RailsApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RubyRuntimeMetaData originalRuntimeMD = new RubyRuntimeMetaData();
        unit.addAttachment( RubyRuntimeMetaData.class, originalRuntimeMD );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( originalRuntimeMD, runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RAILS, runtimeMetaData.getRuntimeType() );
    }

    @Test
    public void testWithExistingTypedRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RailsApplicationMetaData railsAppMetaData = new RailsApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RubyRuntimeMetaData originalRuntimeMD = new RubyRuntimeMetaData();
        originalRuntimeMD.setRuntimeType( RubyRuntimeMetaData.RuntimeType.BARE );

        unit.addAttachment( RubyRuntimeMetaData.class, originalRuntimeMD );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( originalRuntimeMD, runtimeMetaData );
        assertNull( runtimeMetaData.getBaseDir() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }
}
