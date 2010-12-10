package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.deployers.AbstractRubyScanningDeployer;
import org.torquebox.messaging.metadata.TaskMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: suffix and path from jboss-beans.xml
 *   Out: TaskMetaData
 * </pre>
 *
 */
public class TasksScanningDeployer extends AbstractRubyScanningDeployer {

	public TasksScanningDeployer() {
		
	}
	
	@Override
	protected void deploy(VFSDeploymentUnit unit, VirtualFile file, String relativePath) throws DeploymentException {
		log.info( "deploying " + relativePath );
		
		TaskMetaData taskMetaData = new TaskMetaData();
		
		String simpleLocation = getPath() + relativePath.substring( 0, relativePath.length() - 3 );
		
		taskMetaData.setLocation( simpleLocation );
		taskMetaData.setRubyClassName( StringUtils.pathToClassName( relativePath, ".rb" ) );
		
		unit.addAttachment( TaskMetaData.class.getName() + "$" + simpleLocation, taskMetaData, TaskMetaData.class );
	}

}
