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
package org.torquebox.ruby.enterprise.web.rack;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData;
import org.torquebox.ruby.enterprise.web.rack.deployers.RubyRackApplicationPoolDeployer;
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData;
import org.torquebox.ruby.enterprise.web.rack.metadata.RubyRackApplicationMetaData;

public class AppRackYamlParsingDeployer extends AbstractVFSParsingDeployer<RubyRackApplicationMetaData> {

	private Logger log = Logger.getLogger(AppRackYamlParsingDeployer.class);

	private static final ByteList APPLICATION_KEY = ByteList.create("application");
	private static final ByteList WEB_KEY = ByteList.create("web");

	private static final ByteList RACK_ROOT_KEY = ByteList.create("RACK_ROOT");
	private static final ByteList RACK_ENV_KEY = ByteList.create("RACK_ENV");
	private static final ByteList RACKUP_KEY = ByteList.create("rackup");

	private static final ByteList HOST_KEY = ByteList.create("host");
	private static final ByteList CONTEXT_KEY = ByteList.create("context");

	public AppRackYamlParsingDeployer() {
		super(RubyRackApplicationMetaData.class);
		addOutput(RackWebApplicationMetaData.class);
		addOutput(RubyRuntimeMetaData.class);
		setSuffix("-rack.yml");
		setStage(DeploymentStages.REAL);
		// setTopLevelOnly(true);
	}

	@Override
	protected RubyRackApplicationMetaData parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RubyRackApplicationMetaData root)
			throws Exception {
		log.debug("parse( " + vfsUnit + ", " + file + ", " + root + ")");

		if (!file.equals(vfsUnit.getRoot())) {
			log.debug("not deploying non-root: " + file);
			return null;
		}

		Deployment deployment = parseAndSetUp(vfsUnit, file);

		performDeploy(vfsUnit, deployment);

		// Returning null since the RailsMetaData is actually
		// attached as a predetermined managed object on the
		// sub-deployment, and not directly applicable
		// to *this* deployment unit.
		return null;

	}

	@Override
	public void undeploy(DeploymentUnit unit) {
		Deployment deployment = unit.getAttachment("torquebox.rack.root.deployment", Deployment.class);
		if (deployment != null) {
			log.info("Undeploying: " + deployment.getName());
			MainDeployer deployer = unit.getAttachment("torquebox.rack.root.deployer", MainDeployer.class);
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
		unit.addAttachment("torquebox.rack.root.deployment", deployment);
		unit.addAttachment("torquebox.rack.root.deployer", deployer);
	}

	private Deployment createDeployment(VirtualFile rackRoot, RubyRuntimeMetaData runtimeMetaData,
			RubyRackApplicationMetaData rackMetaData, RackWebApplicationMetaData webMetaData) throws MalformedURLException, IOException {
		AbstractVFSDeployment deployment = new AbstractVFSDeployment(rackRoot);

		MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());

		attachments.addAttachment(RubyRackApplicationMetaData.class, rackMetaData);
		attachments.addAttachment(RubyRuntimeMetaData.class, runtimeMetaData);

		if (webMetaData != null) {
			attachments.addAttachment(RackWebApplicationMetaData.class, webMetaData);
		}

		return deployment;
	}

	@SuppressWarnings("unchecked")
	private RubyRackApplicationMetaData parseAndSetUpApplication(VirtualFile rackRootFile, Map<String, Object> config) throws IOException {
		Map<ByteList, Object> application = (Map<ByteList, Object>) config.get(APPLICATION_KEY);
		RubyRackApplicationMetaData rackMetaData = new RubyRackApplicationMetaData();

		rackMetaData.setRackRoot(rackRootFile);

		if (application != null) {

			ByteList rackup = (ByteList) application.get(RACKUP_KEY);

			String rackupScriptPath = null;
			if (rackup != null) {
				rackupScriptPath = rackup.toString();
			} else {
				rackupScriptPath = "config.ru";
			}

			ByteList rackEnv = (ByteList) application.get(RACK_ENV_KEY);

			if (rackEnv != null) {
				rackMetaData.setRackEnv(rackEnv.toString());
			} else {
				rackMetaData.setRackEnv("development");
			}

			VirtualFile rackupFile = rackRootFile.getChild(rackupScriptPath);

			if (rackupFile != null && rackupFile.exists()) {
				StringBuilder rackupScript = new StringBuilder();
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(rackupFile.openStream()));
					String line = null;

					while ((line = in.readLine()) != null) {
						rackupScript.append(line);
						rackupScript.append("\n");
					}
				} finally {
					// rackupFile.closeStreams();
					if (in != null) {
						in.close();
					}
				}

				rackMetaData.setRackUpScript(rackupScript.toString());

			}
		}

		return rackMetaData;

	}

	@SuppressWarnings("unchecked")
	private RackWebApplicationMetaData parseAndSetUpWeb(VirtualFile rackRootFile, Map<String, Object> config) {
		Map<ByteList, Object> web = (Map<ByteList, Object>) config.get(WEB_KEY);

		RackWebApplicationMetaData webMetaData = new RackWebApplicationMetaData();
		if (web != null) {
			ByteList context = (ByteList) web.get(CONTEXT_KEY);
			ByteList host = (ByteList) web.get(HOST_KEY);
			if (host != null) {
				webMetaData.setHost(host.toString());
			}
			if (context != null) {
				webMetaData.setContext(context.toString());
			}
		}

		String appPoolName = RubyRackApplicationPoolDeployer.getBeanName(rackRootFile);
		webMetaData.setRackApplicationPoolName(appPoolName);
		webMetaData.setStaticPathPrefix("/public");
		return webMetaData;
	}

	private RubyRuntimeMetaData parseAndSetUpRuntime(VirtualFile rackRoot, String rackEnv) {
		RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
		runtimeMetaData.setBaseDir(rackRoot);
		RackRuntimeInitializer initializer = new RackRuntimeInitializer(rackRoot, rackEnv);
		runtimeMetaData.setRuntimeInitializer(initializer);
		return runtimeMetaData;
	}

	@SuppressWarnings("unchecked")
	private VirtualFile getRackRoot(Map<String, Object> config) throws IOException {

		Map<ByteList, Object> application = (Map<ByteList, Object>) config.get(APPLICATION_KEY);
		String rackRoot = application.get(RACK_ROOT_KEY).toString();

		VirtualFile rackRootFile = VFS.getChild(rackRoot);
		// TODO close handle on undeploy
		// VFS.mountReal(new File(rackRoot), rackRootFile);

		return rackRootFile;
	}

	@SuppressWarnings("unchecked")
	private Deployment parseAndSetUp(VFSDeploymentUnit unit, VirtualFile file) throws URISyntaxException, IOException {
		Map<String, Object> config = (Map<String, Object>) YAML.load(file.openStream());

		VirtualFile rackRootFile = getRackRoot(config);
		RubyRackApplicationMetaData rackMetaData = parseAndSetUpApplication(rackRootFile, config);
		RackWebApplicationMetaData webMetaData = parseAndSetUpWeb(rackRootFile, config);

		RubyRuntimeMetaData runtimeMetaData = parseAndSetUpRuntime(rackRootFile, rackMetaData.getRackEnv());

		return createDeployment(rackRootFile, runtimeMetaData, rackMetaData, webMetaData);
	}
}
