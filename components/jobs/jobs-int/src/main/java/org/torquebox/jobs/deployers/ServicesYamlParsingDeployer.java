/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.jobs.deployers;

import java.util.Map;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.jobs.core.RubyServiceProxy;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: PARSE
 *    In: services.yml, PoolMetaData
 *   Out: BeanMetaData, PoolMetaData
 * </pre>
 * 
 * Creates BeanMetaData instances from services.yml
 */
public class ServicesYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public static final String POOL_NAME = "services";

    public ServicesYamlParsingDeployer() {
        setSectionName( "services" );
        addInput( PoolMetaData.class );
        addOutput( BeanMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, Map<String, String>> results = (Map<String, Map<String, String>>) dataObj;
        if (results != null) {
            for (String service : results.keySet()) {
                Map<String, String> params = results.get( service );
                createServiceProxyBean( unit, service, params );
            }
        }
        createRuntimePool( unit, results.size() );
    }

    protected void createServiceProxyBean(DeploymentUnit unit, String service, Map params) {
        String beanName = AttachmentUtils.beanName( unit, RubyServiceProxy.class, service );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyServiceProxy.class.getName() );

        ValueMetaData runtimePoolInject = builder.createInject( AttachmentUtils.beanName( unit, RubyRuntimePool.class, POOL_NAME ) );
        builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInject );
        builder.addPropertyMetaData( "rubyComponentResolver", createComponentResolver( service, params ) );

        if (requiresSingleton( params )) {
            builder.addDependency( "jboss.ha:service=HASingletonDeployer,type=Barrier" );
        }

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }

    protected RubyComponentResolver createComponentResolver(String service, Map params) {
        InstantiatingRubyComponentResolver result = new InstantiatingRubyComponentResolver();
        result.setRubyClassName( StringUtils.camelize( service ) );
        result.setRubyRequirePath( StringUtils.underscore( service ) );
        result.setInitializeParams( params );
        result.setComponentName( "service." + service );
        return result;
    }

    protected PoolMetaData createRuntimePool(DeploymentUnit unit, int max) {
        PoolMetaData pool = AttachmentUtils.getAttachment( unit, POOL_NAME, PoolMetaData.class );
        ;
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
