package org.torquebox.rack.deployers;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.dependency.DependencyItemMetaData;
import org.jboss.kernel.spi.dependency.DependencyBuilder;
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

        log.debug( "Deploying virtual hosts: " + unit );
        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );
        
        if ( rackMetaData == null ) {
            return;
        }

        List<String> hosts = new ArrayList<String>();
        hosts.addAll( rackMetaData.getHosts() );

        if (hosts.isEmpty()) {
            log.debug( "No host to deploy" );
            return;
        }

        String canonicalHost = hosts.remove( 0 );
        
        log.debug( "Hosts: " + canonicalHost + " :: " + hosts );

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