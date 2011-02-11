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

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubyRuntimeFactoryDeployerTest extends AbstractDeployerTestCase {

    private RubyRuntimeFactoryDeployer deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new RubyRuntimeFactoryDeployer();
        this.deployer.setKernel( getKernelController().getKernel() );
        addDeployer( this.deployer );
    }

    @Test
    public void testBasics() {
        assertSame( DeploymentStages.CLASSLOADER, this.deployer.getStage() );
        assertSame( RubyRuntimeMetaData.class, this.deployer.getInput() );
        assertTrue( this.deployer.getOutputs().contains( Ruby.class.getName() ) );
    }

    @Test
    public void testDeployment() throws Exception {
        String deploymentName = createDeployment( "runtimeFactory" );

        RubyRuntimeMetaData metaData = new RubyRuntimeMetaData();

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        unit.addAttachment( RubyRuntimeMetaData.class, metaData );

        processDeployments( true );

        RubyRuntimeFactory factory = (RubyRuntimeFactory) getBean( AttachmentUtils.beanName( unit, RubyRuntimeFactory.class ) );

        assertNotNull( factory );
        assertEquals( CompatVersion.RUBY1_8, factory.getRubyVersion() );

        undeploy( deploymentName );
    }

    @Test
    public void testDeploymentWithNonDefaultRubyCompatibilityVersion() throws Exception {
        String deploymentName = createDeployment( "runtimeFactory" );

        RubyRuntimeMetaData metaData = new RubyRuntimeMetaData();
        metaData.setVersion( RubyRuntimeMetaData.Version.V1_9 );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        unit.addAttachment( RubyRuntimeMetaData.class, metaData );

        processDeployments( true );

        RubyRuntimeFactory factory = (RubyRuntimeFactory) getBean( AttachmentUtils.beanName( unit, RubyRuntimeFactory.class ) );

        assertNotNull( factory );
        assertEquals( CompatVersion.RUBY1_9, factory.getRubyVersion() );

        undeploy( deploymentName );
    }

}
