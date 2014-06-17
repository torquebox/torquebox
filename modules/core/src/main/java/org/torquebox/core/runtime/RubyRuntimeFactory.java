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

package org.torquebox.core.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceRegistry;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.ast.executable.Script;
import org.jruby.util.ClassCache;
import org.torquebox.bootstrap.JRubyHomeLocator;
import org.torquebox.core.component.InjectionRegistry;
import org.torquebox.core.pool.InstanceFactory;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.core.util.StringUtils;

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
        this( null, null );
    }

    public RubyRuntimeFactory(RuntimeInitializer initializer) {
        this( initializer, null );
    }

    /**
     * Construct with an initializer and a preparer.
     * 
     * @param initializer
     *            The initializer (or null) to use for each created runtime.
     * @param preparer
     *            The preparer (or null) to use for each created runtime.
     */
    public RubyRuntimeFactory(RuntimeInitializer initializer, RuntimePreparer preparer) {
        this.initializer = initializer;
        this.preparer = preparer;
        if (this.preparer == null) {
            this.preparer = new BaseRuntimePreparer( null );
        }
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
     * Retrieve the profile.api mode
     * 
     * @return Whether the JRuby profile.api flag is enabled or not
     */
    public boolean isProfileApi() {
        return profileApi;
    }

    /**
     * Sets the profile.api value for the JRuby environment.
     * 
     * @param profileApi Whether the JRuby profile.api flag is enabled or not.
     */
    public void setProfileApi(boolean profileApi) {
        this.profileApi = profileApi;
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

        Map<String, String> environment = createEnvironment();
        List<String> argv = prepareJRubyOpts(environment);
        if (this.profileApi) {
            log.info( "JRuby Profile API enabled." );
            argv.add("--profile.api");
        }
        config.processArguments(argv.toArray(new String[argv.size()]));

        config.setLoader( getClassLoader() );
        // config.setClassCache( getClassCache() );
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

        config.setEnvironment( environment );
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

            preparer.prepareRuntime( runtime, contextInfo, this.serviceRegistry );

            log.debug( "Initialize? " + initialize );
            log.debug( "Initializer=" + this.initializer );
            if (initialize) {
                this.injectionRegistry.merge( runtime );
                if (this.initializer != null) {
                    this.initializer.initialize( runtime, contextInfo );
                } else {
                    log.debug( "No initializer set for runtime" );
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
            return internalJRubyHome;
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

    private void logRuntimeDestroyed(RubyInstanceConfig config, String contextInfo) {
        log.info( "Destroyed ruby runtime (ruby_version: " + config.getCompatVersion() + ", compile_mode: "
                + config.getCompileMode() + getFullContext( contextInfo ) + ")" );
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
        RubyInstanceConfig config = instance.getInstanceConfig();
        String contextInfo = (String) instance.getENV().get( "TORQUEBOX_CONTEXT" );
        RuntimeContext.deregisterRuntime( instance );
        if (undisposed.remove( instance )) {
            try {
                RuntimeHelper.evalScriptlet( instance, "ActiveRecord::Base.clear_all_connections! if defined?(ActiveRecord::Base)" );
            } catch (Exception e) {
                // ignorable since we're tearing down the instance anyway
            }
            instance.tearDown( false );
            logRuntimeDestroyed( config, contextInfo );
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

    protected Map<String, String> createEnvironment() {
        Map<String, String> env = new HashMap<String, String>();
        env.putAll( System.getenv() );

        // From javadocs:
        //
        // On UNIX systems the alphabetic case of name is typically significant,
        // while on Microsoft Windows systems it is typically not. For example,
        // the expression System.getenv("FOO").equals(System.getenv("foo")) is
        // likely to be true on Microsoft Windows.
        //
        // This means that if on Windows the env variable is set as Path,
        // we should still retrieve it.
        String path = System.getenv( "PATH" );
        if (path == null) {
            // There is no PATH (or Path) environment variable set,
            // let's create an empty one
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

    private static final List<String> excludedJrubyOptions = Arrays.asList( "--sample", "--client", "--server", "--manage", "--headless" );
    protected List<String> prepareJRubyOpts(Map<String, String> environment) {
        String jrubyOpts = environment.get( "JRUBY_OPTS" );
        List<String> options = StringUtils.parseCommandLineOptions( jrubyOpts );
        // Remove any -Xa.b or -Xa.b.c options since those are expected
        // to already be converted to -Djruby.a.b JVM properties
        Iterator<String> iterator = options.iterator();
        while (iterator.hasNext()) {
            String option = iterator.next();
            if (option.matches( "-X\\w+\\..+" ) || excludedJrubyOptions.contains( option )) {
                iterator.remove();
            }
        }
        return options;
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
        for (Closeable mountedJRubyHome : this.mountedJRubyHomes) {
            try {
                mountedJRubyHome.close();
            } catch (IOException e) {
                // ignore
            }
        }
        this.mountedJRubyHomes.clear();
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

    /** Re-usable preparer. */
    private RuntimePreparer preparer;

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

    /** Whether the JRuby profile api is enabled or not */
    private boolean profileApi = false;

    private ServiceRegistry serviceRegistry;

    private List<Closeable> mountedJRubyHomes = Collections.synchronizedList( new ArrayList<Closeable>() );

    private InjectionRegistry injectionRegistry = new InjectionRegistry();
}
