package org.torquebox.bootstrap;

import java.io.File;
import java.net.URL;

public class JRubyHomeLocator {

    public static String determineJRubyHome() {
        String jrubyHome = null;

        jrubyHome = jrubyHomeViaSysProp();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        jrubyHome = jrubyHomeViaEnv();

        if (jrubyHome != null) {
            return jrubyHome;
        }

        jrubyHome = jrubyHomeRelativeToJBossHome();
        
        if ( jrubyHome != null ) {
            return jrubyHome;
        }
        
        //return jrubyHomeViaClasspath();
        return null;
    }

    public static String jrubyHomeViaEnv() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getenv( "JRUBY_HOME" ) );

        if (jrubyHome != null) {
            return jrubyHome.getAbsolutePath();
        }

        return null;
    }

    public static String jrubyHomeViaSysProp() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getProperty( "jruby.home" ) );

        if (jrubyHome != null && !"true".equals( System.getProperty( "jruby_home.env.ignore" ) )) {
            return jrubyHome.getAbsolutePath();
        }

        return null;
    }

    public static String jrubyHomeRelativeToJBossHome() {
        File jrubyHome = null;
        File jbossHome = ifExists( System.getProperty( "jboss.home.dir" ) );

        if (jbossHome != null) {
            jrubyHome = new File( jbossHome.getParentFile(), "jruby" );
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
