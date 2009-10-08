package org.torquebox.ruby.core.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.runtime.load.ExternalScript;
import org.jruby.runtime.load.JarredScript;
import org.jruby.runtime.load.JavaCompiledScript;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.load.LoadService;
import org.jruby.runtime.load.LoadServiceResource;

public class VFSLoadService extends LoadService {

	private static final Logger log = Logger.getLogger(VFSLoadService.class);

	public VFSLoadService(Ruby runtime) {
		super(runtime);
	}

	@Override
	public void load(String file, boolean wrap) {
		log.debug("load(" + file + ", " + wrap + ")");

		if (!runtime.getProfile().allowLoad(file)) {
			throw runtime.newLoadError("No such file to load -- " + file);
		}

		Library library = null;
		try {
			library = findLibraryExactly(file);
		} catch (MalformedURLException e) {
			throw runtime.newLoadError("URL error -- " + file);
		} catch (URISyntaxException e) {
			throw runtime.newLoadError("URL error -- " + file);
		}

		if (library == null) {
			throw runtime.newLoadError("No such file to load -- " + file);
		}

		try {
			library.load(runtime, wrap);
		} catch (IOException e) {
			throw runtime.newLoadError("IO error -- " + file);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean require(String file) {
		log.debug("require(" + file + ")");
		
		RubyString loadNameRubyString = RubyString.newString(runtime, file);
		if (loadedFeaturesInternal.contains(loadNameRubyString)) {
			log.info( "already loaded [" + file + "]" );
			return false;
		}
		try {
			return super.require(file);
		} catch (Exception e) {
			//log.info("error during super.require(...) " + e.getClass());
			// ignore
		}
		log.debug("not found in super.require(...)");

		Library library = null;

		for (String suffix : LoadService.SuffixType.Both.getSuffixes()) {
			String path = file + suffix;
			log.debug("try [" + path + "]");
			try {
				library = findLibraryExactly(file + suffix);
			} catch (MalformedURLException e) {
				// ignore
			} catch (URISyntaxException e) {
				// ingore
			}
			if (library != null) {
				break;
			}
		}

		log.debug("library=" + library);
		if (library != null) {
			loadedFeaturesInternal.add(loadNameRubyString);
			try {
				log.info("loading " + library);
				library.load(runtime, false);
				return true;
			} catch (IOException e) {
				throw runtime.newLoadError("IO error -- " + file);
			}
		}

		return false;
	}

	protected Library createLibrary(String file, LoadServiceResource resource) {
		if (resource == null) {
			return null;
		}
		if (file.endsWith(".so")) {
			throw runtime.newLoadError("JRuby does not support .so libraries from filesystem");
		} else if (file.endsWith(".jar")) {
			return new JarredScript(resource);
		} else if (file.endsWith(".class")) {
			return new JavaCompiledScript(resource);
		} else {
			return new ExternalScript(resource, file);
		}
	}

	private Library findLibraryExactly(String file) throws MalformedURLException, URISyntaxException {

		log.info("findLibraryExactly(" + file + ")");
		if (file.startsWith("vfszip:") || file.startsWith("vfsfile:")) {
			try {
				VirtualFile virtualFile = VFS.getRoot(new URL(file));
				log.debug("findLibrary() " + virtualFile.toURL());
				LoadServiceResource resource = new LoadServiceResource(virtualFile.toURL(), virtualFile.toURL()
						.toExternalForm());
				// return new ExternalScript(resource, virtualFile.getName());
				return createLibrary(virtualFile.getName(), resource);
			} catch (IOException e) {
				return null;
			}
		} else {
			for (Object eachObj : this.loadPath) {
				String eachPath = (String) eachObj;
				URL url = makeUrl(eachPath, file);

				log.debug("try [" + url + "]");
				if (url != null) {
					try {
						VirtualFile virtualFile = VFS.getRoot(url);
						log.debug("findLibrary() ==> " + virtualFile.toURL());
						LoadServiceResource resource = new LoadServiceResource(virtualFile.toURL(), virtualFile.toURL()
								.toExternalForm());
						return createLibrary(virtualFile.getName(), resource);
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		ClassLoader classLoader = runtime.getInstanceConfig().getLoader();
		URL resourceUrl = classLoader.getResource(file);
		if (resourceUrl != null) {
			LoadServiceResource resource = new LoadServiceResource(resourceUrl, resourceUrl.toExternalForm());
			return createLibrary(resourceUrl.getPath(), resource);
		}

		return null;

	}

	private URL makeUrl(String base, String path) throws MalformedURLException {

		if (base.startsWith("vfszip:") || base.startsWith("vfsfile:")) {
			return new URL(new URL(base), path);
		}

		if (base.startsWith("/")) {
			return new URL(new URL("vfsfile:" + base), path);
		}

		return null;

	}

}
