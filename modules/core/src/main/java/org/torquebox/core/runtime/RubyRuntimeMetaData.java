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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.vfs.VirtualFile;

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
        V1_8, V1_9
    }

    public enum CompileMode {
        FORCE, JIT, OFF
    }

    public enum RuntimeType {
        BARE, RACK, RAILS
    }

    /** Base working directory. */
    private VirtualFile baseDir;

    /** Optional interpreter initializer. */
    private RuntimeInitializer initializer;

    /** Ordered list of paths to add to the Ruby LOAD_PATH. */
    private List<RubyLoadPathMetaData> loadPaths = new LinkedList<RubyLoadPathMetaData>();

    /** Interpreter-specific environment variables. */
    private Map<String, String> environment;

    /** Version of Ruby to use. */
    private Version version = Version.V1_8;

    /** JRuby JIT compile mode to use. */
    private CompileMode compileMode;

    /** The type of runtime this MD is for, if any */
    private RuntimeType runtimeType;

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
    public void setBaseDir(VirtualFile baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Retrieve the base working directory.
     * 
     * @return The base working directory.
     */
    public VirtualFile getBaseDir() {
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
        URL url = getBaseDir().getChild( loadPath ).toURL();
        RubyLoadPathMetaData loadPathMetaData = new RubyLoadPathMetaData( url );
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
}
