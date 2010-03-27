package org.torquebox.ruby.core.deployers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

public abstract class AbstractRubyLoadPathDescriber<T> extends AbstractSimpleVFSRealDeployer<T>{

	public AbstractRubyLoadPathDescriber(Class<T> input) {
		super( input );
		setStage( DeploymentStages.DESCRIBE );
		addInput( RubyRuntimeMetaData.class );
		addOutput( RubyRuntimeMetaData.class );
	}
	
	protected void addLoadPath(VFSDeploymentUnit unit, URL url) {
		
		RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
		
		if ( runtimeMetaData == null ) {
			runtimeMetaData = new RubyRuntimeMetaData();
			unit.addAttachment( RubyRuntimeMetaData.class, runtimeMetaData );
		}
		
		RubyLoadPathMetaData loadPath = new RubyLoadPathMetaData();
		loadPath.setURL( url );
		
		runtimeMetaData.appendLoadPath( loadPath );
	}
	
	protected void addLoadPath(VFSDeploymentUnit unit, VirtualFile file) throws MalformedURLException, URISyntaxException {
		addLoadPath( unit, file.toURL() );
	}
	
	protected void addLoadPath(VFSDeploymentUnit unit, String path) throws IOException, URISyntaxException {
		VirtualFile child = unit.getRoot().getChild( path );
		
		if ( child != null ) {
			addLoadPath( unit, child );
		}
	}
	
	
	

}
