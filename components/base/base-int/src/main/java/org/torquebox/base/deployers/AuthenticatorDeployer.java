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

import java.util.Map;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.dependency.KernelController;
import org.torquebox.auth.UsersRolesAuthenticator;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class AuthenticatorDeployer extends AbstractDeployer
{
    public static final String DEFAULT_AUTH_STRATEGY = "file";
	public static final String DEFAULT_DOMAIN        = "other";
	
    private KernelController controller;

	public AuthenticatorDeployer() {
        setStage(DeploymentStages.REAL);
        setInput(RubyApplicationMetaData.class);
        addOutput(BeanMetaData.class);
    }

	public void setController(KernelController controller) {
		this.controller = controller;
	}

	public KernelController getController() {
		return controller;
	}

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (this.getController() == null) {
			log.error("Unable to configure authentication. No KernelController available");
		} else {
	    	start_authenticators(unit.getAttachment(RubyApplicationMetaData.class));
		}
    }
    
    private void start_authenticators(RubyApplicationMetaData metadata) {
    	Map<String, Map<String, String>> config = metadata.getAuthenticationConfig();
    	if (config == null) { 
    		log.warn("No auth configuration provided. Using defaults");
    		// TODO : Add defaults
    	}
    	for(String key: config.keySet()) {
    		String strategy = config.get(key).get("strategy");
    		String domain   = config.get(key).get("domain");
    		
            if (!strategy.equals("file")) {
                System.err.println("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
            } else {
                UsersRolesAuthenticator authenticator = new UsersRolesAuthenticator();
                authenticator.setAuthDomain(domain);
                String beanName = metadata.getApplicationName() + "-authentication-" + key;
                BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, UsersRolesAuthenticator.class.getName());
                BeanMetaData beanMetaData = builder.getBeanMetaData();
                try {
                	System.out.println("Installing bean: " + beanName);
                    this.getController().install(beanMetaData, authenticator);
                }
                catch (Throwable throwable) {
                    System.err.println("Cannot install PicketBox authentication.");
                    System.err.println(throwable.getMessage());
                    throwable.printStackTrace(System.err);
                }
            }
	
    	}
    }
}
