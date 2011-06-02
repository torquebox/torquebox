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

        // return jrubyHomeViaClasspath();
        return null;
    }

    public static String jrubyHomeViaEnv() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getenv( "JRUBY_HOME" ) );

        System.err.println( "env jrubyHome: " + jrubyHome );

        if (jrubyHome != null) {
            System.err.println( "env jrubyHome.abs: " + jrubyHome.getAbsolutePath() );
            return jrubyHome.getAbsolutePath();
        }

        return null;
    }

    public static String jrubyHomeViaSysProp() {
        File jrubyHome = null;

        jrubyHome = ifExists( System.getProperty( "jruby.home" ) );

        System.err.println( "prop jrubyHome: " + jrubyHome );

        if (jrubyHome != null && !"true".equals( System.getProperty( "jruby_home.env.ignore" ) )) {
            System.err.println( "prop jrubyHome.abs: " + jrubyHome.getAbsolutePath() );
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
