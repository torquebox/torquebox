package org.torquebox.test.mc.vfs;

import org.junit.BeforeClass;
import org.torquebox.test.AbstractTorqueBoxTestCase;

public class AbstractVFSTestCase extends AbstractTorqueBoxTestCase {

    @BeforeClass
    public static void setUpVfs() throws ClassNotFoundException {
        Class.forName( "org.jboss.vfs.VFS" );
    }

}
