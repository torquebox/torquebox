/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.interp.deployers;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;

/** Deploys a bootstrap Ruby interpreter for deployment usage.
 * 
 * <p>Deployment is triggered by a default {@link RubyRuntimeMetaData} attachment.
 * As output, a {@link BeanMetaData} creating a {@link Ruby} from the unit's
 * default {@link RubyRuntimeFactory} is attached.</p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class BootstrapRubyRuntimeDeployer extends AbstractDeployer {

	/** Construct.
	 */
	public BootstrapRubyRuntimeDeployer() {
		setInput( RubyRuntimeMetaData.class );
		addOutput( BeanMetaData.class );
		setStage( DeploymentStages.CLASSLOADER );
	}
	
	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		log.info( "Bootstrapping deployment-time Ruby" );
		
		String rubyBeanName = AttachmentUtils.beanName(unit, Ruby.class, "bootstrap" );
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( rubyBeanName, Ruby.class.getName() );
		
		String factoryBeanName = AttachmentUtils.beanName( unit, RubyRuntimeFactory.class );
		
		ValueMetaData ctorFactory = builder.createValueFactory(factoryBeanName, "create" );
		builder.setConstructorValue( ctorFactory );
		
		AttachmentUtils.attach( unit, builder.getBeanMetaData() );
	}
}