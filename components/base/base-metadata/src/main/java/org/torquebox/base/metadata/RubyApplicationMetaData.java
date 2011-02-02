package org.torquebox.base.metadata;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class RubyApplicationMetaData {
	
    private VirtualFile root;
	private String environmentName = "development";
	private boolean developmentMode = true;
	private boolean archive = false;
	
	public RubyApplicationMetaData() {
	}
	
    public void setRoot(VirtualFile root) {
        this.root = root;
    }

    public void setRoot(String path) {
        if (path != null) {
            String sanitizedPath = null;

            if (path.indexOf("\\\\") >= 0) {
                sanitizedPath = path.replaceAll("\\\\\\\\", "/");
                sanitizedPath = sanitizedPath.replaceAll("\\\\", "");
            } else {
                sanitizedPath = path.replaceAll("\\\\", "/");
            }
            VirtualFile root = VFS.getChild(sanitizedPath);
            setRoot(root);
        }
    }

    public VirtualFile getRoot() {
        return this.root;
    }

    public String getRootPath() {
        try {
            return getRoot().toURL().toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    public void explode(VirtualFile root) {
        this.root = root;
        this.archive = true;
    }

	public void setDevelopmentMode(boolean developmentMode) {
		this.developmentMode = developmentMode;
	}
	
	public boolean isArchive() {
	    return this.archive;
	}
	
	public boolean isDevelopmentMode() {
		return this.developmentMode;
	}
	
	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}
	
	public String getEnvironmentName() {
		return this.environmentName;
	}
	

}
