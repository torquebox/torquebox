package org.torquebox.base.deployers;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.RubyApplication;
import org.torquebox.RubyApplicationMBean;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.jmx.JMXUtils;

public class RubyApplicationDeployer extends AbstractDeployer {
    
    public RubyApplicationDeployer() {
        setInput(RubyApplicationMetaData.class);
        setStage( DeploymentStages.PRE_REAL );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        String beanName = AttachmentUtils.beanName( unit, RubyApplication.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyApplication.class.getName() );
        builder.addPropertyMetaData( "name", rubyAppMetaData.getApplicationName() );
        builder.addPropertyMetaData( "environmentName", rubyAppMetaData.getEnvironmentName() );
        builder.addPropertyMetaData( "rootPath", rubyAppMetaData.getRootPath() );
        
        String mbeanName = JMXUtils.jmxName( "torquebox.apps", rubyAppMetaData.getApplicationName() ).name();
        String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\""+ mbeanName + "\", exposedInterface=" + RubyApplicationMBean.class.getName() + ".class)";
        builder.addAnnotation( jmxAnno );
        
        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }
    
    

}
