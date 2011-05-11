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

package org.torquebox.rack.deployers;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.vfs.VFS;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.injection.InjectableHandlerRegistry;
import org.torquebox.injection.InjectionAnalyzer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RackApplicationFactoryImpl;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;
import org.torquebox.web.rack.RackApplicationFactoryDeployer;

public class RackApplicationFactoryDeployerTest extends AbstractDeployerTestCase {

    private RackApplicationFactoryDeployer deployer;
    private String runtimeInstanceFactoryDeploymentName;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new RackApplicationFactoryDeployer();
        InjectionAnalyzer analyzer = new InjectionAnalyzer();
        analyzer.setInjectableHandlerRegistry( new InjectableHandlerRegistry() );
        this.deployer.setInjectionAnalyzer( analyzer );
        addDeployer( this.deployer );
    }

    @Test
    public void testDefaultPool() throws Exception {
        JavaArchive archive = createJar( "runtime-factory" );
        archive.addResource( getClass().getResource( "runtime-factory-jboss-beans.xml" ), "jboss-beans.xml" );
        File archiveFile = createJarFile( archive );
        this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );

        String deploymentName = createDeployment( "shared" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( VFS.getChild( "/sample/app" ) );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        String beanName = AttachmentUtils.beanName( unit, RackApplicationFactory.class );

        BeanMetaData bmd = getBeanMetaData( unit, beanName );
        assertNotNull( bmd );

        RackApplicationFactory factory = (RackApplicationFactory) getBean( beanName );
        assertNotNull( factory );

        undeploy( deploymentName );
        undeploy( this.runtimeInstanceFactoryDeploymentName );
    }

    @Test
    public void testRackUpScriptLocationExplicit() throws Exception {
        JavaArchive archive = createJar( "runtime-factory" );
        archive.addResource( getClass().getResource( "runtime-factory-jboss-beans.xml" ), "jboss-beans.xml" );
        File archiveFile = createJarFile( archive );
        this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );

        String deploymentName = createDeployment( "with-rackup" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();

        rubyAppMetaData.setRoot( "/sample/app" );
        rackAppMetaData.setRackUpScriptLocation( "config.ru" );

        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        String beanName = AttachmentUtils.beanName( unit, RackApplicationFactory.class );

        BeanMetaData bmd = getBeanMetaData( unit, beanName );
        assertNotNull( bmd );

        RackApplicationFactoryImpl factory = (RackApplicationFactoryImpl) getBean( beanName );
        assertNotNull( factory );
        assertNotNull( factory.getRackUpFile() );
        assertEquals( VFS.getChild( "/sample/app/config.ru" ), factory.getRackUpFile() );

        undeploy( deploymentName );
        undeploy( this.runtimeInstanceFactoryDeploymentName );

    }

    @Test
    public void testRackUpScriptLocationImplicit() throws Exception {
        JavaArchive archive = createJar( "runtime-factory" );
        archive.addResource( getClass().getResource( "runtime-factory-jboss-beans.xml" ), "jboss-beans.xml" );
        File archiveFile = createJarFile( archive );
        this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );

        String deploymentName = createDeployment( "with-rackup" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        rubyAppMetaData.setRoot( VFS.getChild( "/sample/app" ) );

        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );

        processDeployments( true );

        String beanName = AttachmentUtils.beanName( unit, RackApplicationFactory.class );

        BeanMetaData bmd = getBeanMetaData( unit, beanName );
        assertNotNull( bmd );

        RackApplicationFactoryImpl factory = (RackApplicationFactoryImpl) getBean( beanName );
        assertNotNull( factory );
        assertNotNull( factory.getRackUpFile() );
        assertEquals( VFS.getChild( "/sample/app/config.ru" ), factory.getRackUpFile() );

        undeploy( deploymentName );
        undeploy( this.runtimeInstanceFactoryDeploymentName );

    }

}
