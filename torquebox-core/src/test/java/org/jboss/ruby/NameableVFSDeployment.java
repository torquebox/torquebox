
package org.jboss.ruby;

import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.virtual.VirtualFile;

public class NameableVFSDeployment extends AbstractVFSDeployment {
	
	private String name;

	public NameableVFSDeployment(VirtualFile root, String name) {
		super( root );
		this.name = name;
	}

	@Override
	public String getSimpleName() {
		if ( this.name == null ) {
			return super.getSimpleName();
		}
		
		return this.name;
	}
	
}