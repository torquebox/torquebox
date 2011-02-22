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

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.auth.Authenticator;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.mc.AttachmentUtils;

public class AuthenticatorDeployer extends AbstractDeployer
{
    public AuthenticatorDeployer() {
        setStage(DeploymentStages.REAL);
        setInput(RubyApplicationMetaData.class);
        addOutput(BeanMetaData.class);
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        String beanName = AttachmentUtils.beanName(unit, Authenticator.class);
        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, Authenticator.class.getName());

        ValueMetaData kernelControllerInject = builder.createInject("jboss.kernel:service=Kernel", "controller");
        builder.addPropertyMetaData("kernelController", kernelControllerInject);

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(RubyApplicationMetaData.class);
        String authStrategy = rubyAppMetaData.getAuthenticationStrategy();
        if (authStrategy == null || authStrategy.trim().equals("")) {
            authStrategy = Authenticator.DEFAULT_AUTH_STRATEGY;
        }
        
        String authDomain = rubyAppMetaData.getAuthenticationDomain();
        if (authDomain == null || authStrategy.trim().equals("")) {
        	authDomain = Authenticator.DEFAULT_DOMAIN;
        }

        builder.addPropertyMetaData("authStrategy", authStrategy);
        builder.addPropertyMetaData("authDomain", authDomain);
        
        builder.addPropertyMetaData("applicationName", rubyAppMetaData.getApplicationName());

        BeanMetaData beanMetaData = builder.getBeanMetaData();
        AttachmentUtils.attach(unit, beanMetaData);
    }
}
