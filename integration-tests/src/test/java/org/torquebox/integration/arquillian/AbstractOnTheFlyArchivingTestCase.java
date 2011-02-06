package org.torquebox.integration.arquillian;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public abstract class AbstractOnTheFlyArchivingTestCase extends AbstractIntegrationTestCase {

    static protected JavaArchive archive(File directory) {
        return archive( directory, directory.getName() );
    }

    static JavaArchive archive(File directory, String name) {
        JavaArchive archive = ShrinkWrap.create( JavaArchive.class, name );
        importDirectory( archive, directory );
        return archive;
    }

    static void importDirectory(JavaArchive archive, File directory) {
        importDirectory( archive, directory, "" );
    }

    static void importDirectory(JavaArchive archive, File directory, String path) {
        if (!path.equals( "" )) {
            archive.addDirectory( path );
        }

        for (File child : directory.listFiles()) {
            String childPath = null;
            if (path.equals( "" )) {
                childPath = child.getName();
            } else {
                childPath = path + "/" + child.getName();
            }

            if (child.isDirectory()) {
                importDirectory( archive, child, childPath );
            } else {
                archive.addResource( child, childPath );
            }
        }
    }

}
