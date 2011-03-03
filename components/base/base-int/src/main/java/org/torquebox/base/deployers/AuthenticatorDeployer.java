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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.torquebox.mc.AttachmentUtils;

import org.jboss.security.microcontainer.beans.AuthenticationPolicyBean;
import org.jboss.security.microcontainer.beans.metadata.AuthenticationMetaData;
import org.jboss.security.microcontainer.beans.metadata.BaseModuleMetaData;

public class AuthenticatorDeployer extends AbstractDeployer
{
    private Kernel kernel;
    private String applicationName;

    public AuthenticatorDeployer() {
        setStage(DeploymentStages.REAL);
        setInput(RubyApplicationMetaData.class);
        addInput(AuthenticationMetaData.class);
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
            String applicationName = appMetaData.getApplicationName();
            this.setApplicationName(applicationName);
            
            AuthMetaData authMetaData = unit.getAttachment(AuthMetaData.class);
            if (authMetaData != null) { 
                Collection<Config> authConfigs = authMetaData.getConfigurations();
                for(Config config: authConfigs) {
                    installAuthenticator(unit, config.getName(), config.getStrategy(), config.getDomain());
                }
            }
        }
    }
    
    private void installAuthenticator(DeploymentUnit unit, String name, String strategy, String domain) {
    	AuthenticationMetaData jaasMetaData = getOrCreateAuthenticationMetaData(unit);
    	List<BaseModuleMetaData> authModules = new ArrayList<BaseModuleMetaData>();
    	if (jaasMetaData.getModules() != null) {
        	authModules.addAll(jaasMetaData.getModules());
    	}
        String strategyClass = classFor(strategy);
        if (strategyClass != null) {
        	// Create some metadata for the authentication bits
        	BaseModuleMetaData metaData = new BaseModuleMetaData();
        	metaData.setCode(strategyClass);
        	authModules.add(metaData);
        	jaasMetaData.setModules(authModules);
        	
        	// Get our bean metadata and attach it to the DeploymentUnit
            List<BeanMetaData> authBeanMetaData = jaasMetaData.getBeans(domain, AuthenticationPolicyBean.class.getName());
            for (BeanMetaData bmd: authBeanMetaData) {
            	AttachmentUtils.attach(unit, bmd);
            }

            // Set up our authenticator
            String authenticatorBeanName = this.getApplicationName() + "-authentication-" + name;
            BeanMetaDataBuilder authenticatorBuilder = BeanMetaDataBuilderFactory.createBuilder(authenticatorBeanName, UsersRolesAuthenticator.class.getName());
            authenticatorBuilder.addPropertyMetaData("authDomain", domain);            
            log.info("Installing bean: " + authenticatorBeanName);
            AttachmentUtils.attach(unit, authenticatorBuilder.getBeanMetaData());
        }
    }

	private AuthenticationMetaData getOrCreateAuthenticationMetaData( DeploymentUnit unit ) {
		AuthenticationMetaData jaasMetaData = unit.getAttachment(AuthenticationMetaData.class);
    	if (jaasMetaData == null) {
    		jaasMetaData = new AuthenticationMetaData();
    		unit.addAttachment(AuthenticationMetaData.class, jaasMetaData);
    	}
		return jaasMetaData;
	}

    private String classFor(String strategy) {
        if (strategy.equals("file")) {
            return "org.jboss.security.auth.spi.UsersRolesLoginModule.class";
        } 
        System.err.println("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
        return null;
    }
}
