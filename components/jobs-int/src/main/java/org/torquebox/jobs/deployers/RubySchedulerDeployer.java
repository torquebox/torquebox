package org.torquebox.jobs.deployers;

import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.jobs.core.RubyScheduler;
import org.torquebox.jobs.metadata.RubyJobMetaData;

public class RubySchedulerDeployer extends AbstractDeployer {
	
	public RubySchedulerDeployer() {
		setAllInputs( true );
		setStage( DeploymentStages.PRE_REAL );
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends RubyJobMetaData> allMetaData = unit.getAllMetaData( RubyJobMetaData.class );
		
		if ( allMetaData.isEmpty() ) {
			return;
		}
		
		String beanName = getBeanName(unit);
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyScheduler.class.getName() );
		
		builder.addPropertyMetaData( "name", "RubyScheduler$" + unit.getSimpleName() );
		
		BeanMetaData beanMetaData = builder.getBeanMetaData();
		
		unit.addAttachment( BeanMetaData.class.getName() + "$" + RubyScheduler.class.getName(), beanMetaData );
	}
	
	public static String getBeanName(DeploymentUnit unit) {
		return "jboss.ruby.jobs.scheduler." + unit.getSimpleName();
	}

}
