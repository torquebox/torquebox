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

package org.torquebox.auth;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * Authentication bean - integrates with PicketBox to provide JBoss
 * auth bits to ruby apps.
 *
 * @author Lance Ball <lball@redhat.com>
 */
public class Authenticator
{
    public static final String DEFAULT_AUTH_STRATEGY = "file";
    public static final String DEFAULT_DOMAIN        = "other";

    private Kernel kernel;
    private String authStrategy;
    private String authDomain;
    private String applicationName;


    public void setAuthDomain(String authDomain) {
		this.authDomain = authDomain;
	}

	public String getAuthDomain() {
		return authDomain;
	}

	public String getAuthStrategy() {
        if (this.authStrategy == null) { return Authenticator.DEFAULT_AUTH_STRATEGY; }
        return this.authStrategy;
    }

    public void setAuthStrategy(String authStrategy) {
        this.authStrategy = authStrategy;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void start() {
        if (!this.getAuthStrategy().equals("file")) {
            System.err.println("Sorry - don't know how to authenticate with the " + this.authStrategy + " strategy");
        } else {
            UsersRolesAuthenticator authenticator = new UsersRolesAuthenticator();
            authenticator.setAuthDomain(this.getAuthDomain());
            KernelController controller = this.getKernel().getController();
            // TODO: User configured security domain
            BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(this.getApplicationName() + "-authentication-" + this.getAuthDomain(), UsersRolesAuthenticator.class.getName());
            BeanMetaData beanMetaData = builder.getBeanMetaData();
            try {
                controller.install(beanMetaData, authenticator);
            }
            catch (Throwable throwable) {
                System.err.println("Cannot install PicketBox authentication.");
                System.err.println(throwable.getMessage());
                throwable.printStackTrace(System.err);
            }
        }

    }

    public void stop() {
        // release resources
    }

}
