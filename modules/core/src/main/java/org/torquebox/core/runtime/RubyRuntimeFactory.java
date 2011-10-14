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
import java.io.InputStream;
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
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.ast.executable.Script;
import org.jruby.util.ClassCache;
import org.torquebox.bootstrap.JRubyHomeLocator;
import org.torquebox.core.component.ComponentRegistry;
import org.torquebox.core.component.InjectionRegistry;
import org.torquebox.core.pool.InstanceFactory;
import org.torquebox.core.util.JRubyConstants;
import org.torquebox.core.util.RuntimeHelper;

/**
 * Default Ruby runtime interpreter factory implementation.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RubyRuntimeFactory implements InstanceFactory<Ruby> {

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
            return this.classLoader;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl != null) {
            return cl;
        }

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
     * 
     * @param debug
     *            Whether JRuby debug logging should be enabled or not
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Retrieve the debug mode
     * 
     * @return Whether debug logging is enabled or not
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * 
     * @param interactive
     *            Whether the runtime is marked as interactive or not
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Retrieve the interactive mode
     * 
     * @return Whether the runtime is marked as interactive or not
     */
    public boolean isInteractive() {
        return this.interactive;
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
        return createInstance( contextInfo, true );
    }

    public Ruby createInstance(String contextInfo, boolean initialize) throws Exception {

        TorqueBoxRubyInstanceConfig config = new TorqueBoxRubyInstanceConfig();

        config.setLoader( getClassLoader() );
        // config.setClassCache( getClassCache() );
        config.setLoadServiceCreator( new VFSLoadServiceCreator() );
        if (this.rubyVersion != null) {
            config.setCompatVersion( this.rubyVersion );
        }
        if (this.compileMode != null) {
            config.setCompileMode( this.compileMode );
        }
        config.setDebug( this.debug );
        config.setInteractive( this.interactive );

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
        config.setInput( getInput() );
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

            if (initialize) {
                this.injectionRegistry.merge( runtime );
                if (this.initializer != null) {
                    this.initializer.initialize( runtime );
                } else {
                    log.warn( "No initializer set for runtime" );
                }
            }

            performRuntimeInitialization( runtime );
        } catch (Exception e) {
            log.error( "Failed to initialize runtime: ", e );
        } finally {
            if (runtime != null) {
                this.undisposed.add( runtime );
            }

            logRuntimeCreationComplete( config, contextInfo, startTime );
        }

        RuntimeContext.registerRuntime( runtime );

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
        RuntimeContext.deregisterRuntime( instance );
        if (undisposed.remove( instance )) {
            // FIXME: this will be unnecessary after JRUBY-6019 is merged and
            // released
            instance.getBeanManager().unregisterRuntime();

            instance.tearDown( false );
        }
    }

    private void performRuntimeInitialization(Ruby runtime) {
        RuntimeHelper.require( runtime, "org/torquebox/core/runtime/runtime_initialization" );
        defineVersions( runtime );
        setApplicationName( runtime );

    }

    private void defineVersions(Ruby runtime) {
        RuntimeHelper.invokeClassMethod( runtime, "TorqueBox", "define_versions", new Object[] { log } );
    }

    private void setApplicationName(Ruby runtime) {
        RuntimeHelper.invokeClassMethod( runtime, "TorqueBox", "application_name=", new Object[] { applicationName } );
    }

    private void prepareRuntime(Ruby runtime, String contextInfo) {
        if ("1.6.3".equals( JRubyConstants.getVersion() ) ||
                "1.6.4".equals( JRubyConstants.getVersion() )) {
            log.info( "Disabling POSIX ENV passthrough for " + contextInfo + " runtime (TORQUE-497)" );
            StringBuffer env_fix = new StringBuffer();
            env_fix.append( "update_real_env_attr = org.jruby.RubyGlobal::StringOnlyRubyHash.java_class.declared_fields.find { |f| f.name == 'updateRealENV' }\n" );
            env_fix.append( "update_real_env_attr.accessible = true\n" );
            env_fix.append( "update_real_env_attr.set_value(ENV.to_java, false)\n" );
            ;
            RuntimeHelper.evalScriptlet( runtime, env_fix.toString() );
        }

        RuntimeHelper.require( runtime, "rubygems" );
        RuntimeHelper.require( runtime, "torquebox-vfs" );
        RuntimeHelper.require( runtime, "torquebox-core" );
        
        RuntimeHelper.require( runtime, "org/torquebox/core/runtime/thread_context_patch" );
        
        RuntimeHelper.require( runtime, "org/torquebox/core/runtime/activerecord_patch" );

        injectServiceRegistry( runtime );
        ComponentRegistry.createRegistryFor( runtime );
    }

    private void injectServiceRegistry(Ruby runtime) {
        RuntimeHelper.require( runtime, "torquebox/service_registry" );
        RuntimeHelper.invokeClassMethod( runtime, "TorqueBox::ServiceRegistry", "service_registry=", new Object[] { this.serviceRegistry } );
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
     * Set the interpreter input stream.
     * 
     * @param inputStream
     *            The input stream.
     */
    public void setInput(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Retrieve the interpreter input stream.
     * 
     * @return The input stream.
     */
    public InputStream getInput() {
        return this.inputStream;
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

    public Injector<Object> getInjector(String key) {
        return this.injectionRegistry.getInjector( key );
    }

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

    /** Input stream for the interpreter. */
    private InputStream inputStream = System.in;

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

    /** JRuby debug logging enabled or not. */
    private boolean debug = false;

    /** I/O streams setup for interactive use or not */
    private boolean interactive = false;

    private ServiceRegistry serviceRegistry;

    private Closeable mountedJRubyHome;

    private InjectionRegistry injectionRegistry = new InjectionRegistry();
}
