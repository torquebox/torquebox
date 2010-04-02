package org.torquebox.mc.vfs;

import org.junit.BeforeClass;

public class AbstractVFSTestCase {

	@BeforeClass
	public static void setUpVfs() throws ClassNotFoundException {
		Class.forName("org.jboss.vfs.VFS");
	}
	
}
