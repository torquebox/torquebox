package org.torquebox.test;

import java.io.File;

public class AbstractTorqueBoxTestCase {
	
    public boolean isWindows() {
        return System.getProperty( "os.name" ).toLowerCase().matches( ".*windows.*" );
    }
    
    public String vfsAbsolutePrefix() {
    	if ( isWindows() ) {
    		return "/C:";
    	}
    	
    	return "";
    }
    
    public String absolutePrefix() {
    	if ( isWindows() ) {
    		return "C:";
    	}
    	
    	return "";
    }
    
    public String toVfsPath(String path) {
        if ( path.startsWith( "vfs:" ) ) {
            return path;
        }
        
        if ( path.startsWith( "/" ) ) {
            return "vfs:" + path;
        }
        
        if ( path.matches( "^[A-Z]:.*" ) ) {
            return "vfs:/" + path;
        }
        
        
        return toVfsPath( pwd() + File.separator + path );
        
    }
    
    public String pwd() {
        String pwd = System.getProperty( "user.dir" );
        
        if ( isWindows() ) {
            pwd = pwd.substring(0,1).toUpperCase() + pwd.substring(1);
        }
        
        return pwd;
        
    }

}
