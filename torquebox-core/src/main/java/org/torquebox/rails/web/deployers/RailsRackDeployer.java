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
package org.torquebox.rails.web.deployers;

import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.rails.core.metadata.RailsApplicationMetaData;
import org.torquebox.rails.core.metadata.RailsVersionMetaData;
import org.torquebox.ruby.enterprise.web.rack.deployers.RubyRackApplicationFactoryDeployer;
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData;
import org.torquebox.ruby.enterprise.web.rack.metadata.RubyRackApplicationMetaData;

public class RailsRackDeployer extends AbstractSimpleVFSRealDeployer<RailsApplicationMetaData> {

	//private static final Logger log = Logger.getLogger(RailsRackDeployer.class);
	
	private SortedSet<RailsRackUpScriptProvider> providers = new TreeSet<RailsRackUpScriptProvider>();

	public RailsRackDeployer() {
		super(RailsApplicationMetaData.class);
		addInput(RackWebApplicationMetaData.class);
		addInput(RailsVersionMetaData.class);
		addOutput(RackWebApplicationMetaData.class);
		addOutput(RubyRackApplicationMetaData.class);
		setStage(DeploymentStages.POST_PARSE);
	}
	
	@Install
	public void addRailsRackUpScriptProvider(RailsRackUpScriptProvider provider) {
		this.providers.add( provider );
	}
	
	protected RailsRackUpScriptProvider findProvider(RailsVersionMetaData version) {
		RailsRackUpScriptProvider candidate = null;
		
		for ( RailsRackUpScriptProvider each : this.providers ) {
			if ( each.getMajorVersion() > version.getMajor() ) {
				break;
			}
			if ( each.getMinorVersion() > version.getMinor() ) {
				break;
			}
			if ( each.getTinyVersion() > version.getTiny() ) {
				break;
			}
			candidate = each;
		}
		
		return candidate;
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RailsApplicationMetaData railsAppMetaData) throws DeploymentException {

		log.debug("deploying rails rack app");
		RackWebApplicationMetaData rackWebAppMetaData = unit.getAttachment(RackWebApplicationMetaData.class);

		if (rackWebAppMetaData == null) {
			rackWebAppMetaData = new RackWebApplicationMetaData();
			rackWebAppMetaData.setContext("/");
			unit.addAttachment(RackWebApplicationMetaData.class, rackWebAppMetaData);
		}

		//String appFactoryName = "jboss.rack.app." + unit.getSimpleName();
		String appFactoryName = RubyRackApplicationFactoryDeployer.getBeanName( unit );
		rackWebAppMetaData.setRackApplicationFactoryName(appFactoryName);
		rackWebAppMetaData.setStaticPathPrefix( "/public" );

		RubyRackApplicationMetaData rubyRackAppMetaData = new RubyRackApplicationMetaData();
		
		RailsVersionMetaData version = unit.getAttachment(RailsVersionMetaData.class);
		RailsRackUpScriptProvider provider = findProvider( version );
		
		if ( provider == null ) {
			throw new DeploymentException( "Unsupport Rails version: " + version.getVersionString() );
		}
		
		rubyRackAppMetaData.setRackUpScript( provider.getRackUpScript( rackWebAppMetaData.getContext() ) );

		unit.addAttachment(RubyRackApplicationMetaData.class, rubyRackAppMetaData);
		
	}

	/*
	protected String getRackUpScript(String context) {
		if ( context.endsWith( "/" ) ) {
			context = context.substring( 0, context.length() - 1 );
		}
		
		String script = 
			"require %q(org/jboss/rails/web/deployers/rails_rack_dispatcher)\n" +
			"::Rack::Builder.new {\n" + 
			"  run JBoss::Rails::Rack::Dispatcher.new(%q("+ context + "))\n" +
			"}.to_app\n";

		return script;

	}
	*/

}
