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

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;

/**
 * Root configuration for a Ruby interpreter.
 * 
 * <p>
 * A Ruby interpreter is configured with basic information, such as a
 * {@code baseDir} to describe the working directory for execution, a
 * {@link RuntimeInitializer} for performing an interpreter initialization, and
 * a set of load paths to augment the default Ruby {@code LOAD_PATH}.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see RubyLoadPathMetaData
 * 
 */
public class RubyRuntimeMetaData {

    public static final AttachmentKey<RubyRuntimeMetaData> ATTACHMENT_KEY = AttachmentKey.create(RubyRuntimeMetaData.class);
    
    public enum Version {
        V1_8, V1_9, V2_0
    }

    public enum CompileMode {
        FORCE, JIT, OFF
    }

    public enum RuntimeType {
        BARE, RACK, RAILS
    }

    public static final Version DEFAULT_VERSION = Version.V1_8;

    /** Base working directory. */
    private File baseDir;

    /** Optional interpreter initializer. */
    private RuntimeInitializer initializer;

    /** Optional interpreter preparer. */
    private RuntimePreparer preparer;

    /** Ordered list of paths to add to the Ruby LOAD_PATH. */
    private List<RubyLoadPathMetaData> loadPaths = new LinkedList<RubyLoadPathMetaData>();

    /** Interpreter-specific environment variables. */
    private Map<String, String> environment;

    /** Version of Ruby to use. */
    private Version version = null;

    /** JRuby JIT compile mode to use. */
    private CompileMode compileMode;

    /** The type of runtime this MD is for, if any */
    private RuntimeType runtimeType;

    /** Whether JRuby debug logging should be enabled */
    private boolean debug = false;

    /** Whether I/O streams should be setup for interactive use */
    private boolean interactive = false;
    
    /** Whether the JRuby profile API should be turned on */
    private boolean profileApi = false;

    /**
     * Construct.
     */
    public RubyRuntimeMetaData() {
    }

    /**
     * Set the base working directory.
     * 
     * @param baseDir
     *            The base working directory.
     */
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Retrieve the base working directory.
     * 
     * @return The base working directory.
     */
    public File getBaseDir() {
        return this.baseDir;
    }

    /**
     * Set the interpreter initializer.
     * 
     * @param initializer
     *            The initializer.
     */
    public void setRuntimeInitializer(RuntimeInitializer initializer) {
        this.initializer = initializer;
    }

    /**
     * Retrieve the interpreter initializer.
     * 
     * @return The interpreter initializer.
     */
    public RuntimeInitializer getRuntimeInitializer() {
        return this.initializer;
    }
    
    /**
     * Set the interpreter preparer.
     * 
     * @param preparer
     *            The preparer.
     */
    public void setRuntimePreparer(RuntimePreparer preparer) {
        this.preparer = preparer;
    }

    /**
     * Retrieve the interpreter preparer.
     * 
     * @return The interpreter preparer.
     */
    public RuntimePreparer getRuntimePreparer() {
        return this.preparer;
    }

    /**
     * Prepend an element to the {@code LOAD_PATH}.
     * 
     * @param loadPath
     *            The path element to prepend.
     */
    public void prependLoadPath(RubyLoadPathMetaData loadPath) {
        loadPaths.add( 0, loadPath );
    }

    /**
     * Append an element to the {@code LOAD_PATH}.
     * 
     * @param loadPath
     *            The path element to append.
     */
    public void appendLoadPath(RubyLoadPathMetaData loadPath) {
        loadPaths.add( loadPath );
    }
    
    /**
     * Append an element to the {@code LOAD_PATH} relative to the baseDir.
     * 
     * @param loadPath
     *            The relative path element to append
     */
    public void appendLoadPath(String loadPath) throws MalformedURLException {
        RubyLoadPathMetaData loadPathMetaData = new RubyLoadPathMetaData( new File( getBaseDir(), loadPath ) );
        loadPaths.add( loadPathMetaData );
    }

    /**
     * Retrieve the list of {@code LOAD_PATH} elements.
     * 
     * @return The list of path elements.
     */
    public List<RubyLoadPathMetaData> getLoadPaths() {
        return this.loadPaths;
    }

    /**
     * Set interpreter-specific environment variables.
     * 
     * @param environment
     *            The environment variables.
     */
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * Retrieve the interpreter-specific environment variables.
     * 
     * @return The environment variables.
     */
    public Map<String, String> getEnvironment() {
        return this.environment;
    }

    /**
     * Set the version of the Ruby interpreter.
     * 
     * @param version
     *            The version.
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * Retrieve the version of the Ruby interpreter.
     * 
     * @return The version.
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * Retrieve the version of the Ruby interpreter, or the default
     * version if none is set.
     * 
     * @return The version.
     */
    public Version getVersionOrDefault() {
        return this.version == null ? DEFAULT_VERSION : this.version;
    }

    /**
     * Set the compileMode of the Ruby interpreter.
     * 
     * @param compileMode
     *            The compileMode.
     */
    public void setCompileMode(CompileMode compileMode) {
        this.compileMode = compileMode;
    }

    /**
     * Retrieve the compileMode of the Ruby interpreter.
     * 
     * @return The compileMode.
     */
    public CompileMode getCompileMode() {
        return this.compileMode;
    }

    public void setRuntimeType(RuntimeType type) {
        this.runtimeType = type;
    }

    public RuntimeType getRuntimeType() {
        return this.runtimeType;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public boolean isInteractive() {
        return this.interactive;
    }
    

    public boolean isProfileApi() {
        return profileApi;
    }

    public void setProfileApi(boolean profiling) {
        this.profileApi = profiling;
    }    
    
}
