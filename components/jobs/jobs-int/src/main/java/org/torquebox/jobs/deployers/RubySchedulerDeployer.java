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

package org.torquebox.jobs.deployers;

import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.jobs.core.RubyScheduler;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: REAL
 *    In: EnvironmentMetaData, ScheduledJobMetaData
 *   Out: RubyScheduler
 * </pre>
 * 
 * Creates a RubyScheduler bean if there are any job meta data
 */
public class RubySchedulerDeployer extends AbstractDeployer {

    private String runtimePoolName;
    private Kernel kernel;

    public RubySchedulerDeployer() {
        setAllInputs( true );
        addInput( RubyApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.REAL );
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    public void setRubyRuntimePoolName(String runtimePoolName) {
        this.runtimePoolName = runtimePoolName;
    }

    public String getRubyRuntimePoolName() {
        return this.runtimePoolName;
    }

    private void buildScheduler(DeploymentUnit unit, boolean singleton) {
        String beanName = AttachmentUtils.beanName( unit, RubyScheduler.class, singleton ? "Singleton" : null );
        BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyScheduler.class.getName() );

        builder.addPropertyMetaData( "kernel", this.kernel );
        builder.addPropertyMetaData( "name", "RubyScheduler$" + unit.getSimpleName() );

        if (singleton) {
            builder.addDependency( "jboss.ha:service=HASingletonDeployer,type=Barrier" );
        }

        String runtimePoolBeanName = this.runtimePoolName;

        if (runtimePoolBeanName == null) {
            runtimePoolBeanName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, "jobs" );
        }

        ValueMetaData runtimePoolInjection = builder.createInject( runtimePoolBeanName );
        builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInjection );

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }    
    
    // This will tell us if we're running in a clustered environment or not
    public boolean isClustered() {
    	KernelController controller = this.getKernel().getController();
    	if (null == controller) {
    		log.warn("No kernel controller available");
    	} else {
    		return controller.getContext("HASingeltonDeployer", ControllerState.INSTANTIATED, false) != null;
    	}
    	return false;
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {

        Set<? extends ScheduledJobMetaData> allMetaData = unit.getAllMetaData( ScheduledJobMetaData.class );

        if (allMetaData.isEmpty()) { return; }

        DeployedJobTypes jobTypes = getJobTypes(allMetaData);

        if (this.isClustered()) {
            log.debug( "Deploying clustered scheduler: " + unit );
            if ( jobTypes.singletonJobs ) { this.buildScheduler( unit, true  ); }
            if ( jobTypes.regularJobs )   { this.buildScheduler( unit, false ); }
            // Provide info for other deployers down the line (e.g. RubyJobDeployer) that we're clustered
            for (ScheduledJobMetaData each : allMetaData) { each.setClustered( true ); }
        } else {
            log.debug( "Deploying scheduler: " + unit );
            this.buildScheduler( unit, false );
            if (jobTypes.singletonJobs) { log.warn("Can't have singleton jobs in a non-clustered environment."); }
        }
    }

	private DeployedJobTypes getJobTypes(Set<? extends ScheduledJobMetaData> allMetaData) {
		DeployedJobTypes deployedJobTypes = new DeployedJobTypes();
        for (ScheduledJobMetaData each : allMetaData) {
            if (each.isSingleton()) { deployedJobTypes.singletonJobs = true; }
            else { deployedJobTypes.regularJobs = true; }
        }
		return deployedJobTypes;
	}
	
	private class DeployedJobTypes {
		boolean regularJobs   = false;
		boolean singletonJobs = false;
	}

}
