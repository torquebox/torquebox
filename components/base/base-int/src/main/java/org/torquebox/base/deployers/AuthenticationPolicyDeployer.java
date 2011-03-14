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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.security.microcontainer.beans.metadata.ApplicationPolicyMetaDataFactory;
import org.jboss.security.microcontainer.beans.metadata.AuthenticationMetaData;
import org.jboss.security.microcontainer.beans.metadata.BaseModuleMetaData;
import org.jboss.security.microcontainer.beans.metadata.FlaggedModuleMetaData;
import org.jboss.security.microcontainer.beans.metadata.ModuleOptionMetaData;
import org.torquebox.base.metadata.AuthMetaData;
import org.torquebox.base.metadata.AuthMetaData.Config;
import org.torquebox.mc.AttachmentUtils;

public class AuthenticationPolicyDeployer extends AbstractDeployer {

	public AuthenticationPolicyDeployer() {
        setStage(DeploymentStages.POST_PARSE);
        setInput(AuthMetaData.class);
        addOutput(AuthenticationMetaData.class);
        addOutput(BeanMetaData.class);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
        AuthMetaData authMetaData = unit.getAttachment(AuthMetaData.class);
        if (authMetaData != null) {
            Collection<Config> authConfigs = authMetaData.getConfigurations();
            for (Config config : authConfigs) {
                attachPolicy(unit, config);
            }
        }
	}

    private void attachPolicy(DeploymentUnit unit, Config config) {
        String name     = config.getName();
        String strategy = config.getStrategy();
        String domain   = config.getDomain();
        
        String strategyClass = classFor(strategy);
        if (name != null && domain != null && strategyClass != null) {

            AuthenticationMetaData jaasMetaData = new AuthenticationMetaData();
            unit.addAttachment(AuthenticationMetaData.class, jaasMetaData);

            ApplicationPolicyMetaDataFactory factory = new ApplicationPolicyMetaDataFactory();
            factory.setPolicyName(domain);

            // Create some metadata for the authentication bits
            ArrayList<BaseModuleMetaData> authModules = new ArrayList<BaseModuleMetaData>();
            FlaggedModuleMetaData metaData = new FlaggedModuleMetaData();
            metaData.setCode(strategyClass);

            ModuleOptionMetaData usersMetaData = new ModuleOptionMetaData();
            usersMetaData.setName("usersProperties");
            usersMetaData.setValue(config.getUsers());
            ModuleOptionMetaData rolesMetaData = new ModuleOptionMetaData();
            rolesMetaData.setName("rolesProperties");
            rolesMetaData.setValue(config.getRoles());
            
            List<ModuleOptionMetaData> moduleOptions = new ArrayList<ModuleOptionMetaData>();
            moduleOptions.add(usersMetaData);
            moduleOptions.add(rolesMetaData);
            metaData.setModuleOptions(moduleOptions);

            authModules.add(metaData);
            jaasMetaData.setModules(authModules);
            factory.setAuthentication(jaasMetaData);

            // Get our bean metadata and attach it to the DeploymentUnit
            List<BeanMetaData> authBeanMetaData = factory.getBeans();
            for (BeanMetaData bmd : authBeanMetaData) {
                BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(bmd);
                builder.addPropertyMetaData("usersProperties", config.getUsers());
                builder.addPropertyMetaData("rolesProperties", config.getRoles());
                BeanMetaData theRealBeanMetaData = builder.getBeanMetaData();
                log.info("Attaching JAAS BeanMetaData: " + theRealBeanMetaData.getName() + " - " + theRealBeanMetaData.getBean());
                AttachmentUtils.attach(unit, theRealBeanMetaData);
            }
        } else {
        	log.warn("TorqueBox authentication configuration error. Skipping auth deployment.");
        }
    }
    
    private String classFor(String strategy) {
        String result = null;
        if (strategy == null) {
            log.warn("No authentication strategy supplied.");
        } else if (strategy.equals("file")) {
            result = "org.torquebox.auth.SimpleLoginModule.class";
        } else {
            log.warn("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
        }
        return result;
    }

}
