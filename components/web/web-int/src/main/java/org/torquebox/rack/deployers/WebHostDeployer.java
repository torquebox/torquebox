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

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.WebHost;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class WebHostDeployer extends AbstractDeployer {

    public WebHostDeployer() {
        setStage( DeploymentStages.REAL );
        setInput( RackApplicationMetaData.class );
        setAllInputs( true );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {

        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );
        
        if ( rackMetaData == null ) {
            return;
        }

        List<String> hosts = new ArrayList<String>();
        hosts.addAll( rackMetaData.getHosts() );

        if (hosts.isEmpty()) {
            return;
        }

        String canonicalHost = hosts.remove( 0 );
        
        String beanName = AttachmentUtils.beanName( unit, WebHost.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, WebHost.class.getName() );

        builder.addPropertyMetaData( "name", canonicalHost );
        builder.addPropertyMetaData( "aliases", hosts );

        ValueMetaData mbeanServerInject = builder.createInject( "JMXKernel", "mbeanServer" );
        builder.addPropertyMetaData( "MBeanServer", mbeanServerInject );

        builder.addDependency( "WebServer" );

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }
}
