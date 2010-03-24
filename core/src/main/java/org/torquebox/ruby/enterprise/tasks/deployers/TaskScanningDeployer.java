package org.torquebox.ruby.enterprise.tasks.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.common.util.StringUtils;
import org.torquebox.ruby.core.deployers.AbstractRubyScanningDeployer;
import org.torquebox.ruby.enterprise.tasks.TaskMetaData;

public class TaskScanningDeployer extends AbstractRubyScanningDeployer {

	public TaskScanningDeployer() {
		
	}
	
	@Override
	protected void deploy(VFSDeploymentUnit unit, VirtualFile file, String relativePath) throws DeploymentException {
		log.info( "deploying " + relativePath );
		
		TaskMetaData taskMetaData = new TaskMetaData();
		
		taskMetaData.setClassLocation( getPath() + relativePath );
		taskMetaData.setClassName( StringUtils.pathToClassName( relativePath, ".rb" ) );
		
		unit.addAttachment( TaskMetaData.class.getName() + "$" + relativePath, taskMetaData, TaskMetaData.class );
	}

}
