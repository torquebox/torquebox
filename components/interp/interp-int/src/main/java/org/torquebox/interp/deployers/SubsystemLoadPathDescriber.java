package org.torquebox.interp.deployers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.metadata.SubsystemConfiguration;

public class SubsystemLoadPathDescriber extends AbstractDeployer {

	public SubsystemLoadPathDescriber() {
		setAllInputs(true);
		setInput(RubyRuntimeMetaData.class);
		addInput(SubsystemConfiguration.class);
		addOutput(RubyRuntimeMetaData.class);
		setStage(DeploymentStages.DESCRIBE);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (!(unit instanceof VFSDeploymentUnit)) {
			throw new DeploymentException("Deployer only application to VFSDeploymentUnit");
		}

		deploy((VFSDeploymentUnit) unit);
	}

	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(RubyRuntimeMetaData.class);

		if (runtimeMetaData != null) {
			Set<? extends SubsystemConfiguration> configurations = unit.getAllMetaData(SubsystemConfiguration.class);

			for (SubsystemConfiguration config : configurations) {
				log.info("Configurating $LOAD_PATH for subsystem [" + config.getSubsystemName() + "]");
				List<String> loadPaths = config.getLoadPaths();

				if (loadPaths != null) {
					for (String path : loadPaths) {
						try {
							URL url = unit.getRoot().getChild(path).toURL();
							log.info("  url: " + url );
							RubyLoadPathMetaData loadPathMeta = new RubyLoadPathMetaData(url);
							loadPathMeta.setAutoload(true);
							runtimeMetaData.appendLoadPath(loadPathMeta);
						} catch (MalformedURLException e) {
							throw new DeploymentException(e);
						}
					}
				}
			}
		}
	}

}
