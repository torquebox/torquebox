/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.bootstrap;

import java.io.File;

public class JRubyHomeLocator {

    public static String determineJRubyHome(boolean useEnvVar) {
        String jrubyHome = null;

        jrubyHome = jrubyHomeViaSysProp();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        if (useEnvVar) {
            jrubyHome = jrubyHomeViaEnv();

            if (jrubyHome != null) {
                return jrubyHome;
            }
        }

        jrubyHome = jrubyHomeRelativeToJBossHome();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        return null;
    }

    public static String jrubyHomeViaEnv() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getenv( "JRUBY_HOME" ) );

        if (jrubyHome != null && !"true".equals( System.getProperty( "jruby_home.env.ignore" ) )) {
            return jrubyHome.getAbsolutePath();
        }

        return null;
    }

    public static String jrubyHomeViaSysProp() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getProperty( "jruby.home" ) );

        if (jrubyHome != null) {
            return jrubyHome.getAbsolutePath();
        }

        return null;
    }

    public static String jrubyHomeRelativeToJBossHome() {
        File jrubyHome = null;
        File jbossHome = ifExists( System.getProperty( "jboss.home.dir" ) );

        if (jbossHome != null) {
            // look for jruby/ next to $JBOSS_HOME
            jrubyHome = new File( jbossHome.getParentFile(), "jruby" );
            if (jrubyHome.exists() && jrubyHome.isDirectory()) {
                return jrubyHome.getAbsolutePath();
            }
            // look for jruby/ inside $JBOSS_HOME
            jrubyHome = new File(jbossHome, "jruby" );
            if (jrubyHome.exists() && jrubyHome.isDirectory()) {
                return jrubyHome.getAbsolutePath();
            }
        }

        return null;
    }

    public static File ifExists(String path) {
        if (path == null) {
            return null;
        }

        if (path.trim().equals( "" )) {
            return null;
        }

        File file = new File( path );

        if (file.exists() && file.isDirectory()) {
            return file;
        }

        return null;
    }
}
