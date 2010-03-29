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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rack.metadata.RackWebApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.yaml.snakeyaml.Yaml;

public class AppRailsYamlParsingDeployer extends AbstractVFSParsingDeployer<RailsApplicationMetaData> {

	private Logger log = Logger.getLogger(AppRailsYamlParsingDeployer.class);

	private static final String APPLICATION_KEY = "application";
	private static final String WEB_KEY = "web";
	private static final String SIP_KEY = "sip";

	private static final String RAILS_ROOT_KEY = "RAILS_ROOT";
	private static final String RAILS_ENV_KEY = "RAILS_ENV";

	private static final String HOST_KEY = "host";
	private static final String CONTEXT_KEY = "context";

	private static final String RUBYCONTROLLER_KEY = "rubycontroller";

	public AppRailsYamlParsingDeployer() {
		super(RailsApplicationMetaData.class);
		addOutput(RackWebApplicationMetaData.class);
		//addOutput(SipApplicationMetaData.class);
		setSuffix("-rails.yml");
		setStage(DeploymentStages.REAL);
		// setTopLevelOnly(true);
	}

	@Override
	protected RailsApplicationMetaData parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RailsApplicationMetaData root) throws Exception {

		if (!file.equals(vfsUnit.getRoot())) {
			log.debug("not deploying non-root: " + file);
			return null;
		}

		Deployment deployment = parseAndSetUp(file);

		performDeploy(vfsUnit, deployment);

		// Returning null since the RailsMetaData is actually
		// attached as a predetermined managed object on the
		// sub-deployment, and not directly applicable
		// to *this* deployment unit.
		return null;

	}

	@Override
	public void undeploy(DeploymentUnit unit) {
		Deployment deployment = unit.getAttachment("torquebox.rails.root.deployment", Deployment.class);
		if (deployment != null) {
			MainDeployer deployer = unit.getAttachment("torquebox.rails.root.deployer", MainDeployer.class);
			try {
				deployer.removeDeployment(deployment);
				deployer.process();
			} catch (DeploymentException e) {
				log.error(e);
			}
		}
	}

	private void performDeploy(DeploymentUnit unit, Deployment deployment) throws DeploymentException {
		MainDeployer deployer = unit.getMainDeployer();
		deployer.addDeployment(deployment);
		deployer.process();
		deployer.checkComplete(deployment);
		unit.addAttachment("torquebox.rails.root.deployment", deployment);
		unit.addAttachment("torquebox.rails.root.deployer", deployer);
	}

	//private Deployment createDeployment(RailsApplicationMetaData railsMetaData, RackWebApplicationMetaData webMetaData, SipApplicationMetaData sipMetaData) throws MalformedURLException, IOException {
	private Deployment createDeployment(RailsApplicationMetaData railsMetaData, RackWebApplicationMetaData webMetaData) throws MalformedURLException, IOException {
		AbstractVFSDeployment deployment = new AbstractVFSDeployment(railsMetaData.getRailsRoot());

		MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());

		attachments.addAttachment(RailsApplicationMetaData.class, railsMetaData);

		if (webMetaData != null) {
			attachments.addAttachment(RackWebApplicationMetaData.class, webMetaData);
		}

		/*
		if (sipMetaData != null) {
			attachments.addAttachment(SipApplicationMetaData.class, sipMetaData);
		}
		*/

		return deployment;
	}

	@SuppressWarnings("unchecked")
	private Deployment parseAndSetUp(VirtualFile file) throws URISyntaxException, IOException {
		InputStream in = null;
		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Object> results = (Map<String, Object>) yaml.load(in);

			Map<String, Object> application = (Map<String, Object>) results.get(APPLICATION_KEY);
			Map<String, Object> web = (Map<String, Object>) results.get(WEB_KEY);
			//Map<String, Object> sip = (Map<String, Object>) results.get(SIP_KEY);

			RailsApplicationMetaData railsMetaData = new RailsApplicationMetaData();

			if (application != null) {
				String railsRoot = application.get(RAILS_ROOT_KEY).toString();
				String railsEnv = application.get(RAILS_ENV_KEY).toString();
				
				VirtualFile railsRootFile = VFS.getChild(railsRoot);
				// TODO close handle on undeploy
				// VFS.mountReal(new File(railsRoot), railsRootFile );
				
				railsMetaData.setRailsRoot(railsRootFile);
				if (railsEnv != null) {
					railsMetaData.setRailsEnv(railsEnv.toString());
				}
			}

			RackWebApplicationMetaData webMetaData = null;

			if (web != null) {
				String context = (String) web.get(CONTEXT_KEY);
				String host    = (String) web.get(HOST_KEY);
				webMetaData = new RackWebApplicationMetaData();
				if (host != null) {
					webMetaData.setHost(host.toString());
				}
				if (context != null) {
					webMetaData.setContext(context.toString());
				}
			}

			/*
			SipApplicationMetaData sipMetaData = null;

			if (sip != null) {
				ByteList rubyController = (ByteList) sip.get(RUBYCONTROLLER_KEY);
				sipMetaData = new SipApplicationMetaData();
				if (rubyController != null) {
					sipMetaData.setRubyController(rubyController.toString());
				}
			}
			*/

			//return createDeployment(railsMetaData, webMetaData, sipMetaData);
			return createDeployment(railsMetaData, webMetaData);

		} finally {
			if ( in != null ) {
				in.close();
			}
		}
	}

}
