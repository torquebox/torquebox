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
package org.torquebox.rack.deployers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.PojoDeployment;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.yaml.snakeyaml.Yaml;

public class AppRackYamlParsingDeployer extends AbstractVFSParsingDeployer<RackApplicationMetaData> {

	private Logger log = Logger.getLogger(AppRackYamlParsingDeployer.class);

	private static final String APPLICATION_KEY = "application";
	private static final String WEB_KEY = "web";

	private static final String RACK_ROOT_KEY = "RACK_ROOT";
	private static final String RACK_ENV_KEY = "RACK_ENV";
	private static final String RACKUP_KEY = "rackup";

	public AppRackYamlParsingDeployer() {
		super(RackApplicationMetaData.class);
		addOutput(BeanMetaData.class);
		setSuffix("-rack.yml");
		setStage(DeploymentStages.PARSE);
	}

	@Override
	protected RackApplicationMetaData parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RackApplicationMetaData root) throws Exception {
        log.info("JC: parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RackApplicationMetaData root)");

		/*
		if (!file.equals(vfsUnit.getRoot())) {
			log.debug("not deploying non-root: " + file);
			return null;
		}
		*/

		Deployment deployment = parseAndSetUp(vfsUnit, file);

		if (deployment == null) {
			throw new DeploymentException("Unable to parse: " + file);
		}

		attachPojoDeploymentBeanMetaData(vfsUnit, deployment);
		return null;

	}

	protected void attachPojoDeploymentBeanMetaData(VFSDeploymentUnit unit, Deployment deployment) {
		String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, unit.getSimpleName());

		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, PojoDeployment.class.getName());

		ValueMetaData deployerInject = builder.createInject("MainDeployer");

		builder.addConstructorParameter(DeployerClient.class.getName(), deployerInject);
		builder.addConstructorParameter(VFSDeployment.class.getName(), deployment);

		AttachmentUtils.attach(unit, builder.getBeanMetaData());
	}

	private Deployment createDeployment(VirtualFile rackRoot, RubyRuntimeMetaData runtimeMetaData, PoolMetaData poolMetaData, RackApplicationMetaData rackMetaData)
			throws MalformedURLException, IOException {
		AbstractVFSDeployment deployment = new AbstractVFSDeployment(rackRoot);

		MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());

		attachments.addAttachment(RackApplicationMetaData.class, rackMetaData);
		attachments.addAttachment(RubyRuntimeMetaData.class, runtimeMetaData);
		attachments.addAttachment(PoolMetaData.class, poolMetaData);
		
		return deployment;
	}

	@SuppressWarnings("unchecked")
	private RackApplicationMetaData parseAndSetUpApplication(VFSDeploymentUnit unit, VirtualFile rackRootFile, Map<String, Object> config) throws IOException {
		Map<String, Object> application = (Map<String, Object>) config.get(APPLICATION_KEY);
		RackApplicationMetaData rackMetaData = new RackApplicationMetaData();

		rackMetaData.setRackRoot(rackRootFile);

		if (application != null) {

			String rackup = (String) application.get(RACKUP_KEY);

			String rackupScriptPath = null;
			if (rackup != null) {
				rackupScriptPath = rackup;
			} else {
				rackupScriptPath = "config.ru";
			}

			String rackEnv = (String) application.get(RACK_ENV_KEY);

			if (rackEnv != null) {
				rackMetaData.setRackEnv(rackEnv.toString());
			} else {
				rackMetaData.setRackEnv("development");
			}

			rackMetaData.setRackUpScript( rackRootFile.getChild(rackupScriptPath) );
		}
		
		Map<String, Object> web = (Map<String, Object>) config.get(WEB_KEY);
		WebYamlParsingDeployer.parse( unit, web, rackMetaData );
		
		return rackMetaData;

	}

	private RubyRuntimeMetaData parseAndSetUpRuntime(VirtualFile rackRoot, String rackEnv, Map<String, Object> config) {
		RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
		runtimeMetaData.setBaseDir(rackRoot);
		
		Map<String,String> env = (Map<String, String>) config.get( "environment" );
		runtimeMetaData.setEnvironment( env );
		
		RackRuntimeInitializer initializer = new RackRuntimeInitializer(rackRoot, rackEnv);
		runtimeMetaData.setRuntimeInitializer(initializer);
		return runtimeMetaData;
	}
	
	private PoolMetaData setUpPoolMetaData() {
		PoolMetaData poolMetaData = new PoolMetaData("web");
		poolMetaData.setShared();
		return poolMetaData;
	}

	@SuppressWarnings("unchecked")
	private VirtualFile getRackRoot(Map<String, Object> config) throws IOException {

		Map<String, Object> application = (Map<String, Object>) config.get(APPLICATION_KEY);
		String rackRoot = application.get(RACK_ROOT_KEY).toString();

		VirtualFile rackRootFile = VFS.getChild(rackRoot);
		// TODO close handle on undeploy
		// VFS.mountReal(new File(rackRoot), rackRootFile);

		return rackRootFile;
	}

	@SuppressWarnings("unchecked")
	private Deployment parseAndSetUp(VFSDeploymentUnit unit, VirtualFile file) throws URISyntaxException, IOException {
		Yaml yaml = new Yaml();

		Map<String, Object> config = (Map<String, Object>) yaml.load(file.openStream());

		if (config == null) {
			return null;
		}

		VirtualFile rackRootFile = getRackRoot(config);
		RackApplicationMetaData rackMetaData = parseAndSetUpApplication(unit, rackRootFile, config);

		RubyRuntimeMetaData runtimeMetaData = parseAndSetUpRuntime(rackRootFile, rackMetaData.getRackEnv(), config);
		PoolMetaData poolMetaData = setUpPoolMetaData();

		return createDeployment(rackRootFile, runtimeMetaData, poolMetaData, rackMetaData );
	}
}
