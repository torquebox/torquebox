/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.ruby.core.runtime;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.Version;
import org.jboss.beans.metadata.api.annotations.Create;
import org.jboss.kernel.Kernel;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.util.ClassCache;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory;
import org.torquebox.ruby.core.runtime.spi.RuntimeInitializer;

public class DefaultRubyRuntimeFactory implements RubyRuntimeFactory {

	private Kernel kernel;
	private RuntimeInitializer initializer;

	private ClassLoader classLoader;
	private ClassCache<?> classCache;
	private String applicationName;
	private List<String> loadPaths;

	public DefaultRubyRuntimeFactory() {
		this(null);
	}

	public DefaultRubyRuntimeFactory(RuntimeInitializer initializer) {
		this.initializer = initializer;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationName() {
		return this.applicationName;
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	public Kernel getKernel() {
		return this.kernel;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Create(ignored = true)
	public synchronized Ruby create() throws Exception {
		RubyInstanceConfig config = new RubyInstanceConfig();

		//DefaultRubyDynamicClassLoader childLoader = this.classLoader.createChild();
		//config.setLoader(childLoader);

		if (this.classCache == null) {
			this.classCache = new ClassCache<Object>(this.classLoader);
		}
		config.setClassCache(classCache);
		config.setLoadServiceCreator( new VFSLoadServiceCreator() );

		String jrubyHome = null;

		jrubyHome = System.getProperty("jruby.home");

		if (jrubyHome == null) {
			jrubyHome = System.getenv("JRUBY_HOME");
		}

		if (jrubyHome == null) {
			String jbossHome = System.getProperty("jboss.home");

			if (jbossHome != null) {
				File candidatePath = new File(jbossHome, "../jruby");
				if (candidatePath.exists() && candidatePath.isDirectory()) {
					jrubyHome = candidatePath.getAbsolutePath();
				}
			}

		}

		if (jrubyHome == null) {
			String binJruby = RubyInstanceConfig.class.getResource("/META-INF/jruby.home/bin/jruby").toURI()
					.getSchemeSpecificPart();
			jrubyHome = binJruby.substring(0, binJruby.length() - 10);
		}

		if (jrubyHome != null) {
			config.setJRubyHome(jrubyHome);
		}

		config.setEnvironment(getEnvironment());
		config.setOutput(getOutput());
		config.setError(getError());

		List<String> loadPath = new ArrayList<String>();
		loadPath.add("META-INF/jruby.home/lib/ruby/site_ruby/1.8");
		loadPath.addAll( this.loadPaths );

		Ruby runtime = JavaEmbedUtils.initialize(loadPath, config);

		if (this.initializer != null) {
			this.initializer.initialize(runtime);
		}
		injectKernel(runtime);
		setUpConstants(runtime, this.applicationName);
		return runtime;
	}

	private void setUpConstants(Ruby runtime, String applicationName) {
		runtime.evalScriptlet("require %q(org/torquebox/ruby/core/runtime/runtime_constants)\n");
		RubyModule jbossModule = runtime.getClassFromPath("JBoss");
		JavaEmbedUtils.invokeMethod(runtime, jbossModule, "setup_constants", new Object[] { Version.getInstance(),
				applicationName }, void.class);
	}

	private void injectKernel(Ruby runtime) {
		runtime.evalScriptlet("require %q(org/torquebox/ruby/core/runtime/kernel)");
		RubyModule jbossKernel = runtime.getClassFromPath("TorqueBox::Kernel");
		JavaEmbedUtils.invokeMethod(runtime, jbossKernel, "kernel=", new Object[] { this.kernel }, void.class);
	}

	public Map<Object, Object> getEnvironment() {
		Map<Object,Object> env = new HashMap<Object,Object>();
		env.putAll( System.getenv() );
		String path = (String) env.get( "PATH" );
		if ( path == null ) {
			env.put( "PATH", "" );
		}
		return env;
	}

	public PrintStream getOutput() {
		return System.out;
	}

	public PrintStream getError() {
		return System.err;
	}

	public void setLoadPaths(List<String> loadPaths) {
		this.loadPaths = loadPaths;
	}
}
