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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.rails.core.metadata.RailsApplicationMetaData;
import org.torquebox.ruby.enterprise.web.rack.deployers.RubyRackApplicationPoolDeployer;
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData;
import org.torquebox.ruby.enterprise.web.rack.metadata.RubyRackApplicationMetaData;

public class RailsRackDeployer extends AbstractSimpleVFSRealDeployer<RailsApplicationMetaData> {

	//private static final Logger log = Logger.getLogger(RailsRackDeployer.class);
	
	public RailsRackDeployer() {
		super(RailsApplicationMetaData.class);
		addInput(RackWebApplicationMetaData.class);
		addOutput(RackWebApplicationMetaData.class);
		addOutput(RubyRackApplicationMetaData.class);
		setStage(DeploymentStages.POST_PARSE);
	}
	
	@Override
	public void deploy(VFSDeploymentUnit unit, RailsApplicationMetaData railsAppMetaData) throws DeploymentException {

		RackWebApplicationMetaData rackWebAppMetaData = unit.getAttachment(RackWebApplicationMetaData.class);

		if (rackWebAppMetaData == null) {
			rackWebAppMetaData = new RackWebApplicationMetaData();
			rackWebAppMetaData.setContext("/");
			unit.addAttachment(RackWebApplicationMetaData.class, rackWebAppMetaData);
		}

		String appPoolName = RubyRackApplicationPoolDeployer.getBeanName( unit );
		rackWebAppMetaData.setRackApplicationPoolName(appPoolName);
		rackWebAppMetaData.setStaticPathPrefix( "/public" );

		RubyRackApplicationMetaData rubyRackAppMetaData = new RubyRackApplicationMetaData();
		
		String rackUpScript = getRackUpScript( rackWebAppMetaData.getContext() );
		
		rubyRackAppMetaData.setRackUpScript( rackUpScript );

		unit.addAttachment(RubyRackApplicationMetaData.class, rubyRackAppMetaData);
	}
	
	protected String getRackUpScript(String context) {
		if ( context.endsWith( "/" ) ) {
			context = context.substring( 0, context.length() - 1 );
		}
		return "TORQUEBOX_RACKUP_CONTEXT=%q(" + context + ")\n" +
		       "require %q(org/torquebox/rails/web/deployers/rackup.rb)\n" +
		       "run TorqueBox::Rails.app\n";
			
	}

}
