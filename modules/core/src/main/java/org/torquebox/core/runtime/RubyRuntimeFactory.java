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

package org.torquebox.core.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyModule;
import org.jruby.ast.executable.Script;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.util.ClassCache;
import org.torquebox.bootstrap.JRubyHomeLocator;
import org.torquebox.core.pool.InstanceFactory;

/**
 * Default Ruby runtime interpreter factory implementation.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RubyRuntimeFactory implements InstanceFactory<Ruby> {

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

    /** Re-usable initializer. */
    private RuntimeInitializer initializer;

    /** ClassLoader for interpreter. */
    private ClassLoader classLoader;

    /** Shared interpreter class cache. */
    private ClassCache<Script> classCache;

    /** Application name. */
    private String applicationName;

    /** Load paths for the interpreter. */
    private List<String> loadPaths;

    /** Output stream for the interpreter. */
    private PrintStream outputStream = System.out;

    /** Error stream for the interpreter. */
    private PrintStream errorStream = System.err;

    /** JRUBY_HOME. */
    private String jrubyHome;

    /** GEM_PATH. */
    private String gemPath;

    /** Should environment $JRUBY_HOME be considered? */
    private boolean useJRubyHomeEnvVar = true;

    /** Additional application environment variables. */
    private Map<String, String> applicationEnvironment;

    /** Undisposed runtimes created by this factory. */
    private Set<Ruby> undisposed = Collections.synchronizedSet( new HashSet<Ruby>() );

    /** Ruby compatibility version. */
    private CompatVersion rubyVersion;

    /** JRuby compile mode. */
    private CompileMode compileMode;

    private ServiceRegistry serviceRegistry;

    private Closeable mountedJRubyHome;

    /**
     * Construct.
     */
    public RubyRuntimeFactory() {
        this( null );
    }

    /**
     * Construct with an initializer.
     * 
     * @param initializer
     *            The initializer (or null) to use for each created runtime.
     */
    public RubyRuntimeFactory(RuntimeInitializer initializer) {
        this.initializer = initializer;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setGemPath(String gemPath) {
        this.gemPath = gemPath;
    }

    public String getGemPath() {
        return this.gemPath;
    }

    public void setJRubyHome(String jrubyHome) {
        this.jrubyHome = jrubyHome;
    }

    public String getJRubyHome() {
        return this.jrubyHome;
    }

    public void setUseJRubyHomeEnvVar(boolean useJRubyEnvVar) {
        this.useJRubyHomeEnvVar = useJRubyEnvVar;
    }

    public boolean useJRubyHomeEnvVar() {
        return this.useJRubyHomeEnvVar;
    }

    /**
     * Set the interpreter classloader.
     * 
     * @param classLoader
     *            The classloader.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Retrieve the interpreter classloader.
     * 
     * @return The classloader.
     */
    public ClassLoader getClassLoader() {
        if (this.classLoader != null) {
            log.info( "Using configured classload: " + this.classLoader );
            return this.classLoader;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl != null) {
            log.info( "using TCCL" );
            return cl;
        }

        log.info( "Using our own classloader" );
        return getClass().getClassLoader();
    }

    /**
     * Set the Ruby compatibility version.
     * 
     * @param rubyVersion
     *            The version.
     */
    public void setRubyVersion(CompatVersion rubyVersion) {
        this.rubyVersion = rubyVersion;
    }

    /**
     * Retrieve the Ruby compatibility version.
     * 
     * @return The version.
     */
    public CompatVersion getRubyVersion() {
        return this.rubyVersion;
    }

    /**
     * Set the compile mode.
     * 
     * @param compileMode
     *            The mode.
     */
    public void setCompileMode(CompileMode compileMode) {
        this.compileMode = compileMode;
    }

    /**
     * Retrieve the compile mode.
     * 
     * @return The mode.
     */
    public CompileMode getCompileMode() {
        return this.compileMode;
    }

    /**
     * Set the application-specific environment additions.
     * 
     * @param applicationEnvironment
     *            The environment.
     */
    public void setApplicationEnvironment(Map<String, String> applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }

    /**
     * Retrieve the application-specific environment additions.
     * 
     * @return The environment.
     */
    public Map<String, String> getApplicationEnvironment() {
        return this.applicationEnvironment;
    }

    /**
     * Create a new instance of a fully-initialized runtime.
     */
    public Ruby createInstance(String contextInfo) throws Exception {

        RubyInstanceConfig config = new TorqueBoxRubyInstanceConfig();

        config.setLoader( getClassLoader() );
        // config.setClassCache( getClassCache() );
        config.setLoadServiceCreator( new VFSLoadServiceCreator() );
        if (this.rubyVersion != null) {
            config.setCompatVersion( this.rubyVersion );
        }
        if (this.compileMode != null) {
            config.setCompileMode( this.compileMode );
        }

        String jrubyHome = this.jrubyHome;

        if (jrubyHome == null) {
            jrubyHome = JRubyHomeLocator.determineJRubyHome( this.useJRubyHomeEnvVar );

            if (jrubyHome == null) {
                jrubyHome = attemptMountJRubyHomeFromClassPath();
            }
        }

        if (jrubyHome != null) {
            config.setJRubyHome( jrubyHome );
        }

        config.setEnvironment( createEnvironment() );
        config.setOutput( getOutput() );
        config.setError( getError() );

        List<String> loadPath = new ArrayList<String>();
        if (this.loadPaths != null) {
            loadPath.addAll( this.loadPaths );
        }

        config.setLoadPaths( loadPath );

        long startTime = logRuntimeCreationStart( config, contextInfo );

        Ruby runtime = null;
        try {
            runtime = Ruby.newInstance( config );
            runtime.getLoadService().require( "java" );

            prepareRuntime( runtime, contextInfo );

            if (this.initializer != null) {
                ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader( runtime.getJRubyClassLoader().getParent() );
                    this.initializer.initialize( runtime );
                } finally {
                    Thread.currentThread().setContextClassLoader( originalCl );
                }

            } else {
                log.warn( "No initializer set for runtime" );
            }

            performRuntimeInitialization( runtime );
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error( "Failed to initialize runtime: ", ex );
        } finally {
            if (runtime != null) {
                this.undisposed.add( runtime );
            }

            logRuntimeCreationComplete( config, contextInfo, startTime );
        }

        return runtime;
    }

    private String attemptMountJRubyHomeFromClassPath() throws URISyntaxException, IOException {
        String internalJRubyHome = RubyInstanceConfig.class.getResource( "/META-INF/jruby.home" ).toURI().getSchemeSpecificPart();

        if (internalJRubyHome.startsWith( "file:" ) && internalJRubyHome.contains( "!/" )) {
            int slashLoc = internalJRubyHome.indexOf( '/' );
            int bangLoc = internalJRubyHome.indexOf( '!' );

            String jarPath = internalJRubyHome.substring( slashLoc, bangLoc );

            String extraPath = internalJRubyHome.substring( bangLoc + 1 );

            VirtualFile vfsJar = VFS.getChild( jarPath );

            if (vfsJar.exists()) {
                if (!vfsJar.isDirectory()) {
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool( 1 );
                    TempFileProvider tempFileProvider = TempFileProvider.create( "jruby.home", executor );
                    this.mountedJRubyHome = VFS.mountZip( vfsJar, vfsJar, tempFileProvider );
                }

                if (vfsJar.isDirectory()) {
                    VirtualFile vfsJrubyHome = vfsJar.getChild( extraPath );
                    if (vfsJrubyHome.exists()) {
                        return vfsJrubyHome.toURL().toExternalForm();
                    }
                }
            }
        }

        return null;
    }

    private long logRuntimeCreationStart(RubyInstanceConfig config, String contextInfo) {
        log.info( "Creating ruby runtime (ruby_version: " + config.getCompatVersion() + ", compile_mode: " + config.getCompileMode() + getFullContext( contextInfo )
                + ")" );
        return System.currentTimeMillis();
    }

    private void logRuntimeCreationComplete(RubyInstanceConfig config, String contextInfo, long startTime) {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        double elapsedSeconds = Math.floor( (elapsedMillis * 1.0) / 10.0 ) / 100;
        log.info( "Created ruby runtime (ruby_version: " + config.getCompatVersion() + ", compile_mode: " + config.getCompileMode() + getFullContext( contextInfo )
                + ") in " + elapsedSeconds + "s" );
    }

    protected String getFullContext(String contextInfo) {
        String fullContext = null;

        if (this.applicationName != null) {
            fullContext = "app: " + this.applicationName;
        }

        if (contextInfo != null) {
            if (fullContext != null) {
                fullContext += ", ";
            } else {
                fullContext = "";
            }

            fullContext += "context: " + contextInfo;
        }

        if (fullContext == null) {
            fullContext = "";
        } else {
            fullContext = ", " + fullContext;
        }

        return fullContext;
    }

    public synchronized void destroyInstance(Ruby instance) {
        if (undisposed.remove( instance )) {
            instance.tearDown( false );
        }
    }

    private void performRuntimeInitialization(Ruby runtime) {
        runtime.evalScriptlet( "require %q(org/torquebox/core/runtime/runtime_initialization)\n" );
        defineVersions( runtime );
        setApplicationName( runtime );

    }

    private void defineVersions(Ruby runtime) {
        RubyModule torqueBoxModule = runtime.getClassFromPath( "TorqueBox" );
        JavaEmbedUtils.invokeMethod( runtime, torqueBoxModule, "define_versions", new Object[] { log }, void.class );
    }

    private void setApplicationName(Ruby runtime) {
        RubyModule torqueBoxModule = runtime.getClassFromPath( "TorqueBox" );
        JavaEmbedUtils.invokeMethod( runtime, torqueBoxModule, "application_name=", new Object[] { applicationName }, void.class );

    }

    private void prepareRuntime(Ruby runtime, String contextInfo) {
        log.info( "Disabling POSIX ENV passthrough for " + contextInfo + " runtime (TORQUE-497)" );
        StringBuffer env_fix = new StringBuffer();
        env_fix.append( "update_real_env_attr = org.jruby.RubyGlobal::StringOnlyRubyHash.java_class.declared_fields.find { |f| f.name == 'updateRealENV' }\n" );
        env_fix.append( "update_real_env_attr.accessible = true\n" );
        env_fix.append( "update_real_env_attr.set_value(ENV.to_java, false)\n" );
        runtime.evalScriptlet( env_fix.toString() );
        
        runtime.getLoadService().require( "rubygems" );
        runtime.evalScriptlet( "require %q(torquebox-vfs)" );
        runtime.evalScriptlet( "require %q(torquebox-core)" );
        injectServiceRegistry( runtime );
    }

    private void injectServiceRegistry(Ruby runtime) {
        runtime.evalScriptlet( "require %q(torquebox/service_registry)" );
        RubyModule torqueBoxServiceRegistry = runtime.getClassFromPath( "TorqueBox::ServiceRegistry" );
        JavaEmbedUtils.invokeMethod( runtime, torqueBoxServiceRegistry, "service_registry=", new Object[] { this.serviceRegistry }, void.class );
    }

    protected Map<String, String> createEnvironment() {
        Map<String, String> env = new HashMap<String, String>();
        env.putAll( System.getenv() );
        String path = (String) env.get( "PATH" );
        if (path == null) {
            env.put( "PATH", "" );
        }

        String gemPath = System.getProperty( "gem.path" );

        if (gemPath == null) {
            gemPath = this.gemPath;
        }

        if ("default".equals( gemPath )) {
            env.remove( "GEM_PATH" );
            env.remove( "GEM_HOME" );
            gemPath = null;
        }

        if (gemPath != null) {
            env.put( "GEM_PATH", gemPath );
            env.put( "GEM_HOME", gemPath );
        }
        if (this.applicationEnvironment != null) {
            env.putAll( this.applicationEnvironment );
        }

        return env;
    }

    /**
     * Set the interpreter output stream.
     * 
     * @param outputStream
     *            The output stream.
     */
    public void setOutput(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Retrieve the interpreter output stream.
     * 
     * @return The output stream.
     */
    public PrintStream getOutput() {
        return this.outputStream;
    }

    /**
     * Set the interpreter error stream.
     * 
     * @param errorStream
     *            The error stream.
     */
    public void setError(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * Retrieve the interpreter error stream.
     * 
     * @return The error stream.
     */
    public PrintStream getError() {
        return this.errorStream;
    }

    /**
     * Set the interpreter load paths.
     * 
     * <p>
     * Load paths may be either real filesystem paths or VFS URLs
     * </p>
     * 
     * @param loadPaths
     *            The list of load paths.
     */
    public void setLoadPaths(List<String> loadPaths) {
        this.loadPaths = loadPaths;
    }

    /**
     * Retrieve the interpreter load paths.
     * 
     * @return The list of load paths.
     */
    public List<String> getLoadPaths() {
        return this.loadPaths;
    }

    public void create() {
        this.classCache = new ClassCache<Script>( getClassLoader() );
    }

    public synchronized void destroy() {
        Set<Ruby> toDispose = new HashSet<Ruby>();
        toDispose.addAll( this.undisposed );

        for (Ruby ruby : toDispose) {
            destroyInstance( ruby );
        }
        this.undisposed.clear();
        if (this.mountedJRubyHome != null) {
            try {
                this.mountedJRubyHome.close();
            } catch (IOException e) {
                // ignore
            }
            this.mountedJRubyHome = null;
        }
    }

    public ClassCache getClassCache() {
        return this.classCache;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }
}
