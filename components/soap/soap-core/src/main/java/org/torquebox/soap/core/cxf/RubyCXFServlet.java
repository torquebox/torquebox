/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.soap.core.cxf;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;

public class RubyCXFServlet extends CXFNonSpringServlet {

	private static final long serialVersionUID = -2809395081671794214L;

	private final String KERNEL_NAME = "jboss.kernel:service=Kernel";

	public RubyCXFServlet() {

	}

	@SuppressWarnings("deprecation")
	@Override
	public void loadBus(ServletConfig servletConfig) throws ServletException {
		String busName = servletConfig.getInitParameter("cxf.bus.name");
		Kernel kernel = (Kernel) servletConfig.getServletContext().getAttribute(KERNEL_NAME);
		KernelRegistryEntry entry = kernel.getRegistry().getEntry(busName);
		this.bus = (Bus) entry.getTarget();
		super.loadBus(servletConfig);
	}

}
