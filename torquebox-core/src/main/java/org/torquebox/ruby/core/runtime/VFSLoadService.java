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
		log.info("load(" + file + ", " + wrap + ")");

		if (!runtime.getProfile().allowLoad(file)) {
			throw runtime.newLoadError("No such file to load -- " + file);
		}

		Library library = null;
		try {
			library = findLibrary(file);
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
		log.info("require(" + file + ")");
		if (file.startsWith("vfs")) {
			VirtualFile virtualFile = null;
			for (String suffix : LoadService.SuffixType.Both.getSuffixes()) {
				try {
					String fileUrl = file + suffix;
					log.info("try [" + fileUrl + "]");
					virtualFile = VFS.getRoot(new URL(fileUrl));
					if (virtualFile != null) {
						break;
					}
				} catch (MalformedURLException e) {
					// ignore
				} catch (IOException e) {
					// ignore
				}
			}
			if (virtualFile != null) {
				try {
					LoadServiceResource resource = new LoadServiceResource(virtualFile.toURL(), virtualFile.toURL()
							.toExternalForm());
					Library library = new ExternalScript(resource, virtualFile.getName());
					synchronized (loadedFeaturesInternal) {
						RubyString loadNameRubyString = RubyString.newString(runtime, file);
						if (loadedFeaturesInternal.contains(loadNameRubyString)) {
							return false;
						} else {
							loadedFeaturesInternal.add(loadNameRubyString);
						}
					}
					library.load(runtime, false);
					return true;
				} catch (IOException e) {
					throw runtime.newLoadError("IO error -- " + file);
				} catch (URISyntaxException e) {
					throw runtime.newLoadError("URL error -- " + file);
				}
			}
		}
		return super.require(file);
	}

	private Library findLibrary(String file) throws MalformedURLException, URISyntaxException {

		if (file.startsWith("vfszip")) {
			try {
				VirtualFile virtualFile = VFS.getRoot(new URL(file));
				log.info("findLibrary() ==> " + virtualFile.toURL());
				LoadServiceResource resource = new LoadServiceResource(virtualFile.toURL(), virtualFile.toURL()
						.toExternalForm());
				return new ExternalScript(resource, virtualFile.getName());
			} catch (IOException e) {
				throw runtime.newLoadError("IO error -- " + file);
			}
		} else {
			for (Object eachObj : this.loadPath) {
				String eachPath = (String) eachObj;
				URL url = makeUrl(eachPath, file);

				log.info("try [" + url + "]");
				if (url != null) {
					try {
						VirtualFile virtualFile = VFS.getRoot(url);
						log.info("findLibrary() ==> " + virtualFile.toURL());
						LoadServiceResource resource = new LoadServiceResource(virtualFile.toURL(), virtualFile.toURL()
								.toExternalForm());
						return new ExternalScript(resource, virtualFile.getName());
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		return null;

	}

	private URL makeUrl(String base, String path) throws MalformedURLException {

		if (base.startsWith("vfs")) {
			return new URL(new URL(base), path);
		}

		if (base.startsWith("/")) {
			return new URL(new URL("vfsfile:" + base), path);
		}

		return null;

	}

}
