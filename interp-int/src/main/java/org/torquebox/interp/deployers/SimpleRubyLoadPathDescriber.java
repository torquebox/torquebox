package org.torquebox.interp.deployers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

public class SimpleRubyLoadPathDescriber<T> extends AbstractRubyLoadPathDescriber<T> {
	
	private String path;

	public SimpleRubyLoadPathDescriber(Class<T> input) {
		super( input );
	}
	
	protected void setPath(String path) {
		this.path = path;
	}
	
	protected String getPath() {
		return this.path;
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, T root) throws DeploymentException {
		try {
			addLoadPath(unit, this.path);
		} catch (IOException e) {
			throw new DeploymentException( e );
		} catch (URISyntaxException e) {
			throw new DeploymentException( e );
		}
	}

}
