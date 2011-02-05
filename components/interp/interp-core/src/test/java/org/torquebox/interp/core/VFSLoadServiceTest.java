package org.torquebox.interp.core;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.mc.vfs.AbstractVFSTestCase;

import static org.junit.Assert.*;

public class VFSLoadServiceTest extends AbstractVFSTestCase {

    private VFSLoadService loadService;

    @Before
    public void setUp() {
        this.loadService = new VFSLoadService( null );
    }

    @Test
    public void testMakeUrlNonVfsBaseWithoutSlash() throws Exception {
        String base = "/Users/bob/myapp";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "file:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlNonVfsBaseWithSlash() throws Exception {
        String base = "/Users/bob/myapp/";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "file:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlVfsBaseWithoutSlash() throws Exception {
        String base = "vfs:/Users/bob/myapp";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "vfs:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlVfsBaseWithSlash() throws Exception {
        String base = "vfs:/Users/bob/myapp/";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "vfs:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }
}
