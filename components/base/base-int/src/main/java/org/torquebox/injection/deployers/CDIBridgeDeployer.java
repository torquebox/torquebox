package org.torquebox.injection.deployers;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.weld.integration.deployer.env.BootstrapInfo;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.injection.CDIBridge;
import org.torquebox.mc.AttachmentUtils;

public class CDIBridgeDeployer extends AbstractDeployer {

    public CDIBridgeDeployer() {
        setInput(RubyApplicationMetaData.class);
        addOutput( BootstrapInfo.class );
        setStage( DeploymentStages.PRE_PARSE );
    }
    
    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        String beanName = AttachmentUtils.beanName( unit, CDIBridge.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(  beanName, CDIBridge.class.getName() );
        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
        log.info(  "Deploying CDIBridge" );
        unit.addAttachment( BootstrapInfo.class, new BootstrapInfo() );
    }

}
