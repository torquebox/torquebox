/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.app.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.projectodd.polyglot.test.as.MockServiceBuilder;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.core.runtime.RestartableRubyRuntimePool;
import org.torquebox.core.runtime.processors.RuntimePoolInstaller;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class RuntimePoolInstallerTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new RuntimePoolInstaller() );
    }

    @Test
    public void testMinMaxPool() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        PoolMetaData poolMetaData = new PoolMetaData( "pool_one" );
        poolMetaData.setMinimumSize( 2 );
        poolMetaData.setMaximumSize( 200 );

        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "test-app");
        rubyAppMetaData.attachTo( unit );
        
        deploy( phaseContext );
        
        ServiceName poolServiceName = CoreServices.runtimePoolName( unit, "pool_one" );
        
        MockServiceBuilder<?> poolServiceBuilder = phaseContext.getMockServiceTarget().getMockServiceBuilder( poolServiceName );
        Value<?> poolServiceValue = poolServiceBuilder.getValue();
        RestartableRubyRuntimePool poolOne = (RestartableRubyRuntimePool) poolServiceValue.getValue();

        assertNotNull( poolOne );
        assertEquals( "pool_one", poolOne.getName() );
        assertEquals( 2, poolOne.getMinimumInstances() );
        assertEquals( 200, poolOne.getMaximumInstances() );
    }

    @Test
    public void testSharedPoolWithFactory() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        PoolMetaData poolMetaData = new PoolMetaData( "pool_one" );
        poolMetaData.setInstanceFactoryName( "instance_factory" );
        poolMetaData.setShared();
        
        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "test-app");
        rubyAppMetaData.attachTo( unit );
        
        deploy( phaseContext );
        
        ServiceName poolServiceName = CoreServices.runtimePoolName( unit, "pool_one" );
        
        MockServiceBuilder<?> poolServiceBuilder = phaseContext.getMockServiceTarget().getMockServiceBuilder( poolServiceName );
        Value<?> poolServiceValue = poolServiceBuilder.getValue();
        RestartableRubyRuntimePool poolOne = (RestartableRubyRuntimePool) poolServiceValue.getValue();

        assertNotNull( poolOne );
        assertEquals( "pool_one", poolOne.getName() );
    }

}
