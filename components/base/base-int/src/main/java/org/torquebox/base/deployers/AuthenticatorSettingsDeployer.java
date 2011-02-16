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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.Kernel;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class AuthenticatorSettingsDeployer extends AbstractDeployer
{

    private static final String DEFAULT_AUTHENTICATION_STRATEGY = "file";
    private Kernel kernel;

    public AuthenticatorSettingsDeployer() {
        setStage(DeploymentStages.REAL);
        setInput(RubyApplicationMetaData.class);
        addOutput(RubyApplicationMetaData.class);
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(RubyApplicationMetaData.class);

        if (rubyAppMetaData.getAuthenticationStrategy() == null || rubyAppMetaData.getAuthenticationStrategy().trim().equals("")) {
            rubyAppMetaData.setAuthenticationStrategy(DEFAULT_AUTHENTICATION_STRATEGY);
        }
//        String beanName = AttachmentUtils.beanName(unit, Authenticator.class);
//        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, Authenticator.class.getName());

//        KernelController controller = this.kernel.getController();
//
//        try {
//            controller.install(builder.getBeanMetaData(), factory);
//        }
//        catch (Throwable e) {
//            throw new DeploymentException(e);
//        }
    }
}
