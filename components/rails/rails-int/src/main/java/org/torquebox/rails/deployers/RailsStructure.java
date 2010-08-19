/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.torquebox.rails.deployers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.vdf.AbstractRubyStructureDeployer;
import org.torquebox.metadata.EnvironmentMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

/**
 * StructureDeployer to identify Ruby-on-Rails applications.
 * 
 * @author Bob McWhirter
 */
public class RailsStructure extends AbstractRubyStructureDeployer {

	/**
	 * Construct.
	 */
	public RailsStructure() {
		setRelativeOrder( -1000 );
	}

	public boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {
		boolean recognized = false;
		VirtualFile file = structureContext.getFile();

		ContextInfo context = null;

		try {
			VirtualFile config = file.getChild("config");
			if (config != null) {
				VirtualFile environment = config.getChild("environment.rb");
				if (environment.exists() ) {
					context = createContext(structureContext, new String[] { "config" });

					addRailsApplicationMetaData(structureContext, context);
					addDirectoryOfJarsToClasspath(structureContext, context, "lib/java");
					addPluginJars(structureContext, context);

					recognized = true;
				}
			}
		} catch (IOException e) {
			if (context != null) {
				structureContext.removeChild(context);
			}
			throw new DeploymentException(e);
		} catch (URISyntaxException e) {
			if ( context != null ) {
				structureContext.removeChild(context);
			}
		}

		return recognized;
	}

	protected void addPluginJars(StructureContext structureContext, ContextInfo context) throws IOException {
		VirtualFile root = structureContext.getRoot();
		VirtualFile vendorPlugins = root.getChild("vendor/plugins");
		if (vendorPlugins != null) {
			List<VirtualFile> plugins = vendorPlugins.getChildren();

			for (VirtualFile plugin : plugins) {
				VirtualFile pluginLibJava = plugin.getChild("lib/java");
				addDirectoryOfJarsToClasspath(structureContext, context, pluginLibJava.getPathNameRelativeTo(root));
				List<VirtualFile> jars = vendorPlugins.getChildrenRecursively(JAR_FILTER);
				for (VirtualFile jar : jars) {
					addClassPath(structureContext, jar, true, true, context);
				}
			}
		}
	}

	protected void addRailsApplicationMetaData(StructureContext structureContext, ContextInfo context) throws MalformedURLException, URISyntaxException {
		MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
		RailsApplicationMetaData railsAppMetaData = new RailsApplicationMetaData(structureContext.getRoot() );
		attachments.addAttachment(RailsApplicationMetaData.class, railsAppMetaData);
		
		EnvironmentMetaData envMetaData = new EnvironmentMetaData();
		envMetaData.setEnvironmentName( railsAppMetaData.getRailsEnv() );
		envMetaData.setDevelopmentMode( railsAppMetaData.getRailsEnv().equals( "development" ) );
		attachments.addAttachment(EnvironmentMetaData.class, envMetaData);

	}

	@Override
	protected boolean hasValidName(VirtualFile file) {
		return file.getName().endsWith( ".rails" ) || file.getChild( "config/environment.rb" ).exists();
	}

	@Override
	protected boolean hasValidSuffix(String name) {
		return true;
	}

}
