package org.torquebox.injection.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.weld.integration.deployer.DeployersUtils;
import org.jboss.weld.integration.deployer.env.BootstrapInfo;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.injection.CDIBridge;
import org.torquebox.mc.AttachmentUtils;

public class CDIBridgeDeployer extends AbstractDeployer {

    public CDIBridgeDeployer() {
        setInput( RubyApplicationMetaData.class );
        addOutput( BootstrapInfo.class );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.POST_PARSE );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        
        String beanName = AttachmentUtils.beanName( unit, CDIBridge.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, CDIBridge.class.getName() );
        builder.addConstructorParameter( String.class.getName(), rubyAppMetaData.getApplicationName() );
        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
        
        System.err.println( "CDIBridge-Attach: " + builder.getBeanMetaData() );
        
        unit.addAttachment( BootstrapInfo.class, new BootstrapInfo() );
        unit.addAttachment( DeployersUtils.WELD_DEPLOYMENT_FLAG, Boolean.TRUE, Boolean.class );
        

    }

}
