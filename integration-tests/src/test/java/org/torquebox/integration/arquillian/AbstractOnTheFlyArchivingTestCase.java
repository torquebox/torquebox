/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
