/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.rack.core;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.torquebox.interp.spi.RuntimeInitializer;

/**
 * {@link RuntimeInitializer} for Ruby Rack applications.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackRuntimeInitializer implements RuntimeInitializer {

	/** RACK_ROOT. */
	private VirtualFile rackRoot;

	/** RACK_ENV */
	private String rackEnv;

	/** Construct.
	 * 
	 * @param rackRoot The application's {@code RACK_ROOT}.
	 * @param rackEnv The application's {@code RACK_ENV}.
	 */
	public RackRuntimeInitializer(VirtualFile rackRoot, String rackEnv) {
		this.rackRoot = rackRoot;
		this.rackEnv = rackEnv;
	}

	@Override
	public void initialize(Ruby ruby) throws Exception {
		ruby.evalScriptlet(getInitializerScript());
	}

	/** Create the initializer script.
	 * 
	 * @return The initializer script.
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	protected String getInitializerScript() throws MalformedURLException, URISyntaxException {
		StringBuilder script = new StringBuilder();
		String rackRootPath = this.rackRoot.toURL().toString();
		if (rackRootPath.endsWith("/")) {
			rackRootPath = rackRootPath.substring(0, rackRootPath.length() - 1);
		}

		if (! rackRootPath.startsWith("vfs:/")) {
			if (!rackRootPath.startsWith("/")) {
				rackRootPath = "/" + rackRootPath;
			}
		}
		
		script.append("RACK_ROOT=%q(" + rackRootPath + ")\n");
		script.append("RACK_ENV=%q(" + this.rackEnv + ")\n");
		script.append("ENV['RACK_ROOT']=%q(" + rackRootPath + ")\n");
		script.append("ENV['RACK_ENV']=%q(" + this.rackEnv + ")\n");
		return script.toString();
	}

}
