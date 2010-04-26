package org.torquebox.subsystems;

import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.metadata.AbstractSubsystemConfiguration;

public class BasicSubsystemConfigurationDeployer extends AbstractDeployer {

	private String triggerAttachmentName;
	private String configurationClassName;
	private List<String> loadPaths;

	public BasicSubsystemConfigurationDeployer() {
		setAllInputs( true );
	}
	
	public void setTriggerAttachmentName(String triggerAttachmentName) {
		this.triggerAttachmentName = triggerAttachmentName;
	}
	
	public String getTriggerAttachmentName() {
		return this.triggerAttachmentName;
	}
	
	public void setConfigurationClassName(String configurationClassName) {
		this.configurationClassName = configurationClassName;
	}
	
	public String getConfigurationClassName() {
		return this.configurationClassName;
	}
	
	public void setLoadPaths(List<String> loadPaths) {
		this.loadPaths = loadPaths;
	}
	
	public List<String> getLoadPaths() {
		return this.loadPaths;
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if ( unit.getAttachment( this.triggerAttachmentName ) != null ) {
			try {
				setUpConfiguration( unit );
			} catch (ClassNotFoundException e) {
				throw new DeploymentException( e );
			} catch (InstantiationException e) {
				throw new DeploymentException( e );
			} catch (IllegalAccessException e) {
				throw new DeploymentException( e );
			}
		}
	}

	protected void setUpConfiguration(DeploymentUnit unit) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<AbstractSubsystemConfiguration> configurationClass = (Class<AbstractSubsystemConfiguration>) unit.getClassLoader().loadClass( this.configurationClassName );
		
		AbstractSubsystemConfiguration configuration = configurationClass.newInstance();
		
		if ( this.loadPaths != null ) {
			for ( String path : loadPaths ) {
				configuration.addLoadPath( path );
			}
		}
		
		//unit.addAttachment( configurationClass, configuration );
		unit.addAttachment( configurationClass, configuration );
		
	}
}
