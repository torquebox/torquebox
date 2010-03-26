package org.torquebox.ruby.core.runtime;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyFile;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.load.LoadService;
import org.jruby.runtime.load.LoadServiceResource;
import org.jruby.util.JRubyFile;

public class VFSLoadService extends LoadService {

	// private static final Logger log = Logger.getLogger(VFSLoadService.class);

	{
		for (int i = 0; i < searchers.size(); ++i) {
			if (searchers.get(i) instanceof NormalSearcher) {
				searchers.set(i, new VFSSearcher());
			}
		}
	}

	public class VFSSearcher implements LoadSearcher {

		@Override
		public boolean shouldTrySearch(SearchState state) {
			return state.library == null;
		}

		@Override
		public void trySearch(SearchState state) throws AlreadyLoaded {
			state.library = findLibraryWithoutCWD(state, state.searchFile, state.suffixType);
		}

	}

	public VFSLoadService(Ruby runtime) {
		super(runtime);
	}

	private URL makeUrl(String base, String path) throws MalformedURLException {

		base = base.replaceAll("\\/\\/+", "/");
		path = path.replaceAll("\\/\\/+", "/");
		if (!base.endsWith("/")) {
			base = base + "/";
		}

		if (base.startsWith("vfs:")) {
			return new URL(new URL(base), path);
		}

		if (base.startsWith("/")) {
			return new URL(new URL("file:" + base), path);
		}

		return null;
	}

	protected Library findLibraryWithoutCWD(SearchState state, String baseName, SuffixType suffixType) {
		Library library = null;

		switch (suffixType) {
		case Both:
			library = findBuiltinLibrary(state, baseName, SuffixType.Source);
			if (library == null)
				library = createLibrary(state, tryResourceFromLoadPathOrURL(state, baseName, SuffixType.Source));
			// If we fail to find as a normal Ruby script, we try to find as an
			// extension,
			// checking for a builtin first.
			if (library == null)
				library = findBuiltinLibrary(state, baseName, SuffixType.Extension);
			if (library == null)
				library = createLibrary(state, tryResourceFromLoadPathOrURL(state, baseName, SuffixType.Extension));
			break;
		case Source:
		case Extension:
			// Check for a builtin first.
			library = findBuiltinLibrary(state, baseName, suffixType);
			if (library == null)
				library = createLibrary(state, tryResourceFromLoadPathOrURL(state, baseName, suffixType));
			break;
		case Neither:
			if (library == null)
				library = createLibrary(state, tryResourceFromLoadPathOrURL(state, baseName, SuffixType.Neither));
			break;
		}

		return library;
	}

	protected LoadServiceResource tryResourceFromLoadPathOrURL(SearchState state, String baseName, SuffixType suffixType) {
		LoadServiceResource foundResource = null;

		// if it's a ./ baseName, use CWD logic
		if (baseName.startsWith("./")) {
			foundResource = tryResourceFromCWD(state, baseName, suffixType);

			if (foundResource != null) {
				state.loadName = foundResource.getName();
				return foundResource;
			}
		}

		// if given path is absolute, just try it as-is (with extensions) and no
		// load path
		if (new File(baseName).isAbsolute() || baseName.startsWith("vfs:")) {
			for (String suffix : suffixType.getSuffixes()) {
				String namePlusSuffix = baseName + suffix;
				foundResource = tryResourceAsIs(namePlusSuffix);

				if (foundResource != null) {
					state.loadName = namePlusSuffix;
					return foundResource;
				}
			}

			return null;
		}

		Outer: for (Iterator pathIter = loadPath.getList().iterator(); pathIter.hasNext();) {
			// TODO this is really ineffient, and potentially a problem
			// everytime anyone require's something.
			// we should try to make LoadPath a special array object.
			String loadPathEntry = ((IRubyObject) pathIter.next()).toString();

			if (loadPathEntry.equals(".")) {
				foundResource = tryResourceFromCWD(state, baseName, suffixType);

				if (foundResource != null) {
					state.loadName = foundResource.getName();
					break Outer;
				}
			} else {
				for (String suffix : suffixType.getSuffixes()) {
					String namePlusSuffix = baseName + suffix;
					foundResource = tryResourceFromLoadPath(namePlusSuffix, loadPathEntry);

					if (foundResource != null) {
						state.loadName = namePlusSuffix;
						break Outer; // end suffix iteration
					}
				}
			}
		}

		return foundResource;
	}

	protected LoadServiceResource tryResourceFromLoadPath(String namePlusSuffix, String loadPathEntry) throws RaiseException {
		LoadServiceResource foundResource = null;

		try {
			if (!Ruby.isSecurityRestricted()) {

				if (loadPathEntry.startsWith("vfs:")) {
					try {
						URL vfsUrl = makeUrl(loadPathEntry, namePlusSuffix);
						// VirtualFile file = VFS.getRoot(vfsUrl);
						VirtualFile file = VFS.getChild(vfsUrl);
						if (file != null && file.exists()) {
							return new LoadServiceResource(file.toURI().toURL(), vfsUrl.toExternalForm());
						}
						return null;
					} catch (MalformedURLException e) {
						// log.error( "vfs failure", e );
					} catch (URISyntaxException e) {
						// log.error( "vfs failure", e );
					}
				}

				String reportedPath = loadPathEntry + "/" + namePlusSuffix;
				JRubyFile actualPath = null;
				// we check length == 0 for 'load', which does not use load path
				if (new File(reportedPath).isAbsolute()) {
					// it's an absolute path, use it as-is
					actualPath = JRubyFile.create(loadPathEntry, RubyFile.expandUserPath(runtime.getCurrentContext(), namePlusSuffix));
				} else {
					// prepend ./ if . is not already there, since we're loading
					// based on CWD
					if (reportedPath.charAt(0) != '.') {
						reportedPath = "./" + reportedPath;
					}
					actualPath = JRubyFile.create(JRubyFile.create(runtime.getCurrentDirectory(), loadPathEntry).getAbsolutePath(),
							RubyFile.expandUserPath(runtime.getCurrentContext(), namePlusSuffix));
				}
				if (actualPath.isFile()) {
					try {
						foundResource = new LoadServiceResource(actualPath.toURI().toURL(), reportedPath);
					} catch (MalformedURLException e) {
						throw runtime.newIOErrorFromException(e);
					}
				}
			}
		} catch (SecurityException secEx) {
		}

		return foundResource;
	}

	protected LoadServiceResource tryResourceAsIs(String namePlusSuffix) throws RaiseException {
		LoadServiceResource foundResource = null;

		try {
			if (!Ruby.isSecurityRestricted()) {
				String reportedPath = namePlusSuffix;
				// we check length == 0 for 'load', which does not use load path
				if (reportedPath.startsWith("vfs:")) {
					try {
						URL vfsUrl = new URL(reportedPath);
						// VirtualFile file = VFS.getRoot(vfsUrl);
						VirtualFile file = VFS.getChild(vfsUrl);
						if (file != null && file.exists()) {
							return new LoadServiceResource(file.toURI().toURL(), reportedPath);
						}
					} catch (IOException e) {
						// ignore
					} catch (URISyntaxException e) {
						// ignore
					}
					return null;
				}

				File actualPath = null;
				if (new File(reportedPath).isAbsolute()) {
					actualPath = new File(RubyFile.expandUserPath(runtime.getCurrentContext(), namePlusSuffix));
				} else {
					// prepend ./ if . is not already there, since we're loading
					// based on CWD
					if (reportedPath.charAt(0) == '.' && reportedPath.charAt(1) == '/') {
						reportedPath = reportedPath.replaceFirst("\\./", runtime.getCurrentDirectory());
					}
					actualPath = new File(RubyFile.expandUserPath(runtime.getCurrentContext(), reportedPath));
				}
				if (actualPath.isFile()) {
					try {
						foundResource = new LoadServiceResource(actualPath.toURI().toURL(), reportedPath);
					} catch (MalformedURLException e) {
						throw runtime.newIOErrorFromException(e);
					}
				}
			}
		} catch (SecurityException secEx) {
			// ignore
		}

		return foundResource;
	}

	public void load(String file, boolean wrap) {
		if (!runtime.getProfile().allowLoad(file)) {
			throw runtime.newLoadError("No such file to load -- " + file);
		}

		SearchState state = new SearchState(file);
		state.prepareLoadSearch(file);

		Library library = findBuiltinLibrary(state, state.searchFile, state.suffixType);
		if (library == null)
			library = findLibraryWithoutCWD(state, state.searchFile, state.suffixType);

		if (library == null) {
			library = findLibraryWithClassloaders(state, state.searchFile, state.suffixType);
			if (library == null) {
				throw runtime.newLoadError("No such file to load -- " + file);
			}
		}
		try {
			library.load(runtime, wrap);
		} catch (IOException e) {
			if (runtime.getDebug().isTrue())
				e.printStackTrace(runtime.getErr());
			throw runtime.newLoadError("IO error -- " + file);
		}
	}

}
