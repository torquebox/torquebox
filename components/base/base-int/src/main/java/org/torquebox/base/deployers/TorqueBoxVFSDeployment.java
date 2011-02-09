package org.torquebox.base.deployers;

import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.vfs.VirtualFile;

public class TorqueBoxVFSDeployment extends AbstractVFSDeployment {
    
    private String simpleName;
    
    public TorqueBoxVFSDeployment(String name, VirtualFile root) {
        super( name, root );
        this.simpleName = name;
    }
    
    @Override
    public String getSimpleName() {
        return this.simpleName + ".trq";
    }

}
