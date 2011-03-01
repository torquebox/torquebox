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

package org.torquebox.services.deployers;

import java.util.Map;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.services.RubyServiceProxy;
import org.torquebox.services.ServiceMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: services.yml, PoolMetaData
 *   Out: BeanMetaData, PoolMetaData
 * </pre>
 * 
 * Creates BeanMetaData instances from services.yml
 */
public class ServicesDeployer extends AbstractDeployer {

    public static final String POOL_NAME = "services";

    public ServicesDeployer() {
        addInput( ServiceMetaData.class );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.REAL );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends ServiceMetaData> allMetaData = unit.getAllMetaData( ServiceMetaData.class );
        
        for ( ServiceMetaData each : allMetaData ) {
            deploy( unit, each );
        }
    }
    
    public void deploy(DeploymentUnit unit, ServiceMetaData serviceMetaData) throws DeploymentException {
        String beanName = AttachmentUtils.beanName( unit, RubyServiceProxy.class, serviceMetaData.getClassName() );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyServiceProxy.class.getName() );

        ValueMetaData runtimePoolInject = builder.createInject( AttachmentUtils.beanName( unit, RubyRuntimePool.class, POOL_NAME ) );
        
        builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInject );
        builder.addPropertyMetaData( "rubyComponentResolver", createComponentResolver( serviceMetaData ) );

        if ( serviceMetaData.isRequiresSingleton() ) {
            builder.addDependency( "jboss.ha:service=HASingletonDeployer,type=Barrier" );
        }

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }

    protected RubyComponentResolver createComponentResolver(ServiceMetaData serviceMetaData) {
        InstantiatingRubyComponentResolver result = new InstantiatingRubyComponentResolver();
        result.setRubyClassName( StringUtils.camelize( serviceMetaData.getClassName() ) );
        result.setRubyRequirePath( StringUtils.underscore( serviceMetaData.getClassName() ) );
        result.setInitializeParams( serviceMetaData.getParameters() );
        result.setComponentName( "service." + serviceMetaData.getClassName() );
        return result;
    }

    protected PoolMetaData createRuntimePool(DeploymentUnit unit, int max) {
        PoolMetaData pool = AttachmentUtils.getAttachment( unit, POOL_NAME, PoolMetaData.class );

        if (pool == null && max > 0) {
            pool = new PoolMetaData( POOL_NAME, 1, max );
            log.info( "Configured Ruby runtime pool for services: " + pool );
            AttachmentUtils.multipleAttach( unit, pool, POOL_NAME );
        }
        return pool;
    }

    protected boolean requiresSingleton(Map params) {
        Boolean singleton = params == null ? null : (Boolean) params.remove( "singleton" );
        return singleton != null && singleton.booleanValue();
    }


}
