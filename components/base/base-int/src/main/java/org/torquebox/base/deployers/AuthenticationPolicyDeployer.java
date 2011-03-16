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
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.ApplicationPolicyRegistration;
import org.torquebox.base.metadata.AuthMetaData;
import org.torquebox.base.metadata.AuthMetaData.Config;

public class AuthenticationPolicyDeployer extends AbstractDeployer {

	public AuthenticationPolicyDeployer() {
        setStage(DeploymentStages.POST_PARSE);
        setInput(AuthMetaData.class);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
        AuthMetaData authMetaData = unit.getAttachment(AuthMetaData.class);
        if (authMetaData != null) {
            Collection<Config> authConfigs = authMetaData.getConfigurations();
            for (Config config : authConfigs) {
                initializePolicy(unit, config);
            }
        }
	}

    private void initializePolicy(DeploymentUnit unit, Config config) {
        String name     = config.getName();
        String strategy = config.getStrategy();
        String domain   = config.getDomain();
        
        String strategyClass = classFor(strategy);
        if (name != null && domain != null && strategyClass != null) {
            Configuration configuration = Configuration.getConfiguration();
            if (configuration instanceof ApplicationPolicyRegistration == false) {
            	log.error("Cannot configure TorqueBox security. Unidentified configuration supplied. " + configuration.getClass().getName());
            } else {
                ApplicationPolicy policy = new ApplicationPolicy(domain);
                AuthenticationInfo authenticationInfo = new AuthenticationInfo();
                Map<String,Object> options = new HashMap<String,Object>();
                options.put("users", config.getUsers());
                options.put("roles", config.getRoles());
                AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry(strategyClass, LoginModuleControlFlag.REQUIRED, options);
                authenticationInfo.addAppConfigurationEntry(appConfigurationEntry);
                policy.setAuthenticationInfo(authenticationInfo);
                ApplicationPolicyRegistration applicationPolicyRegistration = (ApplicationPolicyRegistration) configuration;
                applicationPolicyRegistration.addApplicationPolicy(domain, policy);
                log.info("Added application auth policy for domain: " + domain);
            }

        } else {
        	log.warn("TorqueBox authentication configuration error. Skipping auth deployment.");
        }
    }
    
    private String classFor(String strategy) {
        String result = null;
        if (strategy == null) {
            log.warn("No authentication strategy supplied.");
        } else if (strategy.equals("simple")) {
            result = "org.torquebox.auth.SimpleLoginModule.class";
        } else if (strategy.equals("file")) {
        	result = "org.jboss.security.auth.spi.UsersRolesLoginModule";
        } else {
            log.warn("Sorry - I don't know how to authenticate with the " + strategy + " strategy yet.");
        }
        return result;
    }

}
