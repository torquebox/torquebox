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

package org.torquebox.base.deployers;

import java.util.Collection;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.Kernel;
import org.torquebox.auth.UsersRolesAuthenticator;
import org.torquebox.base.metadata.AuthMetaData;
import org.torquebox.base.metadata.AuthMetaData.Config;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class AuthenticatorDeployer extends AbstractDeployer
{
    private Kernel kernel;
	private String applicationName;

	public AuthenticatorDeployer() {
        setStage(DeploymentStages.REAL);
        addInput(RubyApplicationMetaData.class);
        addInput(AuthMetaData.class);
        addOutput(BeanMetaData.class);
    }

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	public Kernel getKernel() {
		return kernel;
	}

    public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;		
	}
    
    public String getApplicationName() {
    	return this.applicationName;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (this.getKernel() == null) {
			log.error("Unable to configure authentication. No KernelController available");
		} else {
	    	RubyApplicationMetaData appMetaData = unit.getAttachment(RubyApplicationMetaData.class);
	    	this.setApplicationName(appMetaData.getApplicationName());
	    	
	    	AuthMetaData authMetaData = unit.getAttachment(AuthMetaData.class);
	    	if (authMetaData != null) { 
		    	Collection<Config> authConfigs = authMetaData.getConfigurations();
		    	for(Config config: authConfigs) {
		            installAuthenticator(config.getName(), config.getStrategy(), config.getDomain());
		    	}
	    	}
		}
    }
    
	private void installAuthenticator(String name, String strategy, String domain) {
		if (!strategy.equals("file")) {
		    System.err.println("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
		} else {
		    UsersRolesAuthenticator authenticator = new UsersRolesAuthenticator();
		    authenticator.setAuthDomain(domain);
		    String beanName = this.getApplicationName() + "-authentication-" + name;
		    BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, UsersRolesAuthenticator.class.getName());
		    BeanMetaData beanMetaData = builder.getBeanMetaData();
		    try {
		    	log.info("Installing bean: " + beanName);
		        this.getKernel().getController().install(beanMetaData, authenticator);
		    }
		    catch (Throwable throwable) {
		        log.error("Cannot install PicketBox authentication.");
		        log.error(throwable.getMessage());
		        throwable.printStackTrace(System.err);
		    }
		}
	}
}
