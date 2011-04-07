package org.torquebox.bootstrap;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppsDirectoryBootstraperTest {
    
    @Test
    public void testSanitizedUnixPath() {
        AppsDirectoryBootstrapper bootstrapper = new AppsDirectoryBootstrapper();
        
        String path = "/path/to/foo";
        assertEquals( "/path/to/foo", bootstrapper.getSanitizedPath( path ) );
    }
    
    @Test
    public void testSanitizedWindowsPath() {
        AppsDirectoryBootstrapper bootstrapper = new AppsDirectoryBootstrapper();
        
        String path = "C:\\path\\to\\crazy town\\";
        assertEquals( "/C:/path/to/crazy town/", bootstrapper.getSanitizedPath( path ) );
    }

}
