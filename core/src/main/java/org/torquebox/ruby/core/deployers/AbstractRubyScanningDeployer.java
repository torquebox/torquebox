package org.torquebox.ruby.core.deployers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;
import org.torquebox.ruby.core.runtime.metadata.RubyLoadPathMetaData;
import org.torquebox.ruby.core.runtime.metadata.RubyRuntimeMetaData;

public abstract class AbstractRubyScanningDeployer extends AbstractDeployer {

	private String path;
	private VirtualFileFilter filter;
	private boolean addToLoadPath;

	public AbstractRubyScanningDeployer() {
		setStage( DeploymentStages.PARSE );
	}

	public void setPath(String path) {
		setPath( path, true );
	}
	
	public void setPath(String path, boolean addToLoadPath) {
		this.path = path;
		this.addToLoadPath = addToLoadPath;
	}

	public String getPath() {
		return this.path;
	}
	
	public void setAddToLoadPath(boolean addToLoadPath) {
		this.addToLoadPath = addToLoadPath;
	}
	
	public boolean isAddToLoadPath() {
		return this.addToLoadPath;
	}

	public void setFilter(VirtualFileFilter filter) {
		this.filter = filter;
	}
	
	public void setSuffixFilter(String suffix) {
		this.filter =  new SuffixMatchFilter( suffix, VisitorAttributes.DEFAULT);
		
	}

	public VirtualFileFilter getFilter() {
		return this.filter;
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (!(unit instanceof VFSDeploymentUnit)) {
			throw new DeploymentException("Deployment unit must be a VFSDeploymentUnit");
		}

		deploy((VFSDeploymentUnit) unit);
	}

	protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		try {
			VirtualFile scanRoot = unit.getRoot().getChild(this.path);

			if (scanRoot == null) {
				return;
			}
			
			if ( this.isAddToLoadPath() ) {
				RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
				if ( runtimeMetaData == null ) {
					runtimeMetaData = new RubyRuntimeMetaData();
					unit.addAttachment( RubyRuntimeMetaData.class, runtimeMetaData );
				}
				RubyLoadPathMetaData loadPath = new RubyLoadPathMetaData( scanRoot.toURL() );
				runtimeMetaData.appendLoadPath( loadPath );
			}
			
			List<VirtualFile> children = null;
			
			if ( this.filter != null ) {
				children = scanRoot.getChildrenRecursively( this.filter );
			} else {
				children = scanRoot.getChildrenRecursively();
			}
			
			int prefixLength = scanRoot.getPathName().length();
			
			for ( VirtualFile child : children ) {
				String relativePath = child.getPathName().substring( prefixLength );
				deploy( unit, child, relativePath.substring(1) );
			}

		} catch (IOException e) {
			throw new DeploymentException(e);
		} catch (URISyntaxException e) {
			throw new DeploymentException(e);
		}
	}
	
	protected abstract void deploy(VFSDeploymentUnit unit, VirtualFile file, String relativePath) throws DeploymentException;

}
