package org.torquebox.core.app;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jboss.as.server.deployment.DeploymentException;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.DefaultRubyRuntimePool;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.runtime.RuntimePoolDeployer;
import org.torquebox.core.runtime.SharedRubyRuntimePool;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;
import org.torquebox.test.as.MockServiceBuilder;

public class RuntimePoolDeployerTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new RuntimePoolDeployer() );
    }

    @Test
    public void testMinMaxPool() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        PoolMetaData poolMetaData = new PoolMetaData( "pool_one" );
        poolMetaData.setMinimumSize( 2 );
        poolMetaData.setMaximumSize( 200 );

        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "test-app");
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        
        deploy( phaseContext );
        
        ServiceName poolServiceName = CoreServices.runtimePoolName( unit, "pool_one" );
        
        MockServiceBuilder<?> poolServiceBuilder = phaseContext.getMockServiceTarget().getMockServiceBuilder( poolServiceName );
        Value<?> poolServiceValue = poolServiceBuilder.getValue();
        DefaultRubyRuntimePool poolOne = (DefaultRubyRuntimePool) poolServiceValue.getValue();

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
        
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "test-app");
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        
        deploy( phaseContext );
        
        ServiceName poolServiceName = CoreServices.runtimePoolName( unit, "pool_one" );
        
        MockServiceBuilder<?> poolServiceBuilder = phaseContext.getMockServiceTarget().getMockServiceBuilder( poolServiceName );
        Value<?> poolServiceValue = poolServiceBuilder.getValue();
        SharedRubyRuntimePool poolOne = (SharedRubyRuntimePool) poolServiceValue.getValue();

        assertNotNull( poolOne );
        assertEquals( "pool_one", poolOne.getName() );
    }

}
