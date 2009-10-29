package org.torquebox.ruby.enterprise.web.rack;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.jboss.virtual.VirtualFile;
import org.jruby.Ruby;
import org.torquebox.ruby.core.runtime.spi.RuntimeInitializer;

public class RackRuntimeInitializer implements RuntimeInitializer {
	
	private VirtualFile rackRoot;
	private String rackEnv;

	public RackRuntimeInitializer(VirtualFile rackRoot, String rackEnv) {
		this.rackRoot = rackRoot;
		this.rackEnv  = rackEnv;
	}

	@Override
	public void initialize(Ruby ruby) throws Exception {
		ruby.evalScriptlet( getInitializerScript() );
	}
	
	protected String getInitializerScript() throws MalformedURLException, URISyntaxException {
		StringBuilder script = new StringBuilder();
		String rackRootPath = this.rackRoot.toURL().toExternalForm();
		if ( rackRootPath.endsWith( "/" ) ) {
			rackRootPath = rackRootPath.substring( 0, rackRootPath.length() - 1 );
		}
		
		if ( rackRootPath.startsWith( "vfsfile:/" ) ) {
			rackRootPath = rackRootPath.substring( 9 );
			
			if ( ! rackRootPath.startsWith( "/" ) ) {
				rackRootPath = "/" + rackRootPath;
			}
		}
		
		script.append( "RACK_ROOT=%q(" + rackRootPath  +")\n" );
		script.append( "RACK_ENV=%q(" + this.rackEnv + ")\n" );
		script.append( "ENV['RACK_ROOT']=%q(" + rackRootPath +")\n" );
		script.append( "ENV['RACK_ENV']=%q(" + this.rackEnv + ")\n" );
		return script.toString();
	}

}
