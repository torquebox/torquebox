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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.core.BasicRubyRuntimePoolMBean;
import org.torquebox.interp.core.DefaultRubyRuntimePool;
import org.torquebox.interp.core.DefaultRubyRuntimePoolMBean;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.jmx.JMXNameBuilder;
import org.torquebox.mc.jmx.JMXUtils;
import org.torquebox.mc.vdf.AbstractMultipleMetaDataDeployer;

/**
 * <pre>
 * Stage: REAL
 *    In: PoolMetaData, DeployerRuby
 *   Out: RubyRuntimePool
 * </pre>
 * 
 * Creates the proper RubyRuntimePool as specified by the PoolMetaData
 */
public class RuntimePoolDeployer extends AbstractMultipleMetaDataDeployer<PoolMetaData> {

    public RuntimePoolDeployer() {
        super( PoolMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.REAL );
    }

    protected void deploy(DeploymentUnit unit, PoolMetaData poolMetaData) throws DeploymentException {
        String beanName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, poolMetaData.getName() );
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        BeanMetaData poolBean = null;
        BeanMetaDataBuilder builder = null;

        if (poolMetaData.isGlobal()) {
            builder = BeanMetaDataBuilder.createBuilder( beanName, SharedRubyRuntimePool.class.getName() );
            builder.addPropertyMetaData( "name", poolMetaData.getName() );

            String instanceName = poolMetaData.getInstanceName();
            if (instanceName == null) {
                try {
                    Ruby runtime = unit.getAttachment( DeployerRuby.class ).getRuby();
                    builder.addConstructorParameter( Ruby.class.getName(), runtime );
                } catch (Exception e) {
                    throw new DeploymentException( e );
                }
            } else {
                ValueMetaData runtimeInjection = builder.createInject( instanceName );
                builder.addConstructorParameter( Ruby.class.getName(), runtimeInjection );
            }

            JMXNameBuilder jmxName = JMXUtils.jmxName( "torquebox.pools", rubyAppMetaData.getApplicationName() );
            jmxName.with( "name", poolMetaData.getName() );
            jmxName.with( "type", "shared" );
            
            String mbeanName = jmxName.name();
            String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\"" + mbeanName + "\", exposedInterface=" + BasicRubyRuntimePoolMBean.class.getName() + ".class)";
            builder.addAnnotation( jmxAnno );

        } else if (poolMetaData.isShared()) {
            builder = BeanMetaDataBuilder.createBuilder( beanName, SharedRubyRuntimePool.class.getName() );
            builder.addPropertyMetaData( "name", poolMetaData.getName() );
            String factoryName = poolMetaData.getInstanceFactoryName();
            if (factoryName == null) {
                factoryName = AttachmentUtils.beanName( unit, RubyRuntimeFactory.class );
            }
            ValueMetaData factoryInjection = builder.createInject( factoryName );
            builder.addConstructorParameter( RubyRuntimeFactory.class.getName(), factoryInjection );
            
            JMXNameBuilder jmxName = JMXUtils.jmxName( "torquebox.pools", rubyAppMetaData.getApplicationName() );
            jmxName.with( "name", poolMetaData.getName() );
            jmxName.with( "type", "shared" );
            
            String mbeanName = jmxName.name();
            String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\"" + mbeanName + "\", exposedInterface=" + BasicRubyRuntimePoolMBean.class.getName() + ".class)";
            builder.addAnnotation( jmxAnno );
        } else {
            builder = BeanMetaDataBuilder.createBuilder( beanName, DefaultRubyRuntimePool.class.getName() );
            String factoryName = poolMetaData.getInstanceFactoryName();
            if (factoryName == null) {
                factoryName = AttachmentUtils.beanName( unit, RubyRuntimeFactory.class );
            }
            ValueMetaData factoryInjection = builder.createInject( factoryName );
            builder.addConstructorParameter( RubyRuntimeFactory.class.getName(), factoryInjection );
            builder.addPropertyMetaData( "name", poolMetaData.getName() );
            builder.addPropertyMetaData( "minimumInstances", poolMetaData.getMinimumSize() );
            builder.addPropertyMetaData( "maximumInstances", poolMetaData.getMaximumSize() );
            
            JMXNameBuilder jmxName = JMXUtils.jmxName( "torquebox.pools", rubyAppMetaData.getApplicationName() );
            jmxName.with( "name", poolMetaData.getName() );
            jmxName.with( "type", "default" );
            
            String mbeanName = jmxName.name();
            String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\"" + mbeanName + "\", exposedInterface=" + DefaultRubyRuntimePoolMBean.class.getName() + ".class)";
            builder.addAnnotation( jmxAnno );
        }
        
        System.err.println( "attaching: " + builder.getBeanMetaData() );

        poolBean = builder.getBeanMetaData();
        AttachmentUtils.attach( unit, poolBean );
    }

}
