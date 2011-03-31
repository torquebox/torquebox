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

package org.torquebox.interp.deployers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class BaseRubyRuntimeDeployerTest extends AbstractDeployerTestCase {

    private Map<String, String> environment = new HashMap<String, String>();

    @Before
    public void setUpDeployer() throws Throwable {
        addDeployer( new BaseRubyRuntimeDeployer() );
    }

    @Test
    public void testHappy() throws Exception {
        environment.put( "SOME_VAR", "gassy" );
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );
        rubyAppMetaData.setEnvironmentVariables( environment );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey( "SOME_VAR" ) );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }


    @Test
    public void testWithExistingRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RubyRuntimeMetaData existingRuntimeMD = new RubyRuntimeMetaData();
        
        unit.addAttachment( RubyRuntimeMetaData.class, existingRuntimeMD );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( existingRuntimeMD, runtimeMetaData );
        assertEquals( vfsAbsolutePrefix() + "/foo", runtimeMetaData.getBaseDir().getPathName() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }


    @Test
    public void testWithExistingTypedRubyRuntimeMD() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();

        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/foo" ) );

        String deploymentName = createDeployment( "test" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RubyRuntimeMetaData existingRuntimeMD = new RubyRuntimeMetaData();
        existingRuntimeMD.setRuntimeType( RubyRuntimeMetaData.RuntimeType.RACK );

        unit.addAttachment( RubyRuntimeMetaData.class, existingRuntimeMD );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );

        processDeployments( true );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        assertNotNull( runtimeMetaData );
        assertEquals( existingRuntimeMD, runtimeMetaData );
        assertNull( runtimeMetaData.getBaseDir() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RACK, runtimeMetaData.getRuntimeType() );
    }

}
