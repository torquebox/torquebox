package org.torquebox.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.vfs.VirtualFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BootstrapTest {

    private Bootstrap bootstrap;

    @Before
    public void setUpBootstrap() {
        this.bootstrap = new Bootstrap();
    }
    
    @Before
    public void setUpJBossHomeSysProp() {
        System.setProperty( "jboss.home", "C:\\jboss" );
    }
    
    @After
    public void cleanUpStuff() {
        System.clearProperty( "jruby.home" );
    }

    @Test
    public void testDefaultJRubyHome() throws Exception {
        assertEquals( "C:\\jruby", this.bootstrap.getJRubyHomeDefault() );
    }
    
    @Test
    public void testJRubyHomeSysProp() throws Exception {
        System.setProperty( "jruby.home", "C:\\jruby_a" );
        String path = this.bootstrap.getJRubyHomePath();
        assertEquals( "C:\\jruby_a", path );
    }
    
    @Test
    public void testJRubyHomeVirtualFile() throws Exception {
        VirtualFile jrubyHome = this.bootstrap.getJRubyHomeVirtualFile();
        
        assertNotNull( jrubyHome );
    }

}
