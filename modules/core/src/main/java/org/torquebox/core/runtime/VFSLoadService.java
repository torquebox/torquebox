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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyFile;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyString;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.load.LoadService;
import org.jruby.runtime.load.LoadServiceResource;
import org.jruby.util.JRubyFile;

/**
 * VFS-enabled {@link LoadService}
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class VFSLoadService extends LoadService {


    public class VFSSearcher implements LoadSearcher {

        @Override
        public boolean shouldTrySearch(SearchState state) {
            return state.library == null;
        }

        @Override
        public boolean trySearch(SearchState state) {
            state.library = findLibraryWithoutCWD( state, state.searchFile, state.suffixType );
            return true;
        }

    }

    public VFSLoadService(Ruby runtime) {
        super( runtime );
    }

    URL makeUrl(String base, String path) throws MalformedURLException {

        base = base.replaceAll( "\\/\\/+", "/" );
        path = path.replaceAll( "\\/\\/+", "/" );
        if (!base.endsWith( "/" )) {
            base = base + "/";
        }

        if (base.startsWith( "vfs:" )) {
            return new URL( new URL( base ), path );
        }

        if (base.startsWith( "/" )) {
            return new URL( new URL( "file:" + base ), path );
        }

        return null;
    }

    protected Library findLibraryWithoutCWD(SearchState state, String baseName, SuffixType suffixType) {
        //System.err.println("findLibraryWithoutCWD(" + baseName + "," +
         //suffixType + ")");
        Library library = null;

        switch (suffixType) {
        case Both:
            library = findBuiltinLibrary( state, baseName, SuffixType.Source );
            if (library == null)
                library = createLibrary( state, tryResourceFromJarURL( state, baseName, SuffixType.Source ) );
            if (library == null)
                library = createLibrary( state, tryResourceFromLoadPathOrURL( state, baseName, SuffixType.Source ) );
            // If we fail to find as a normal Ruby script, we try to find as an
            // extension,
            // checking for a builtin first.
            if (library == null)
                library = findBuiltinLibrary( state, baseName, SuffixType.Extension );
            if (library == null)
                library = createLibrary( state, tryResourceFromJarURL( state, baseName, SuffixType.Extension ) );
            if (library == null)
                library = createLibrary( state, tryResourceFromLoadPathOrURL( state, baseName, SuffixType.Extension ) );
            break;
        case Source:
        case Extension:
            // Check for a builtin first.
            library = findBuiltinLibrary( state, baseName, suffixType );
            if (library == null)
                library = createLibrary( state, tryResourceFromJarURL( state, baseName, suffixType ) );
            if (library == null)
                library = createLibrary( state, tryResourceFromLoadPathOrURL( state, baseName, suffixType ) );
            break;
        case Neither:
            library = createLibrary( state, tryResourceFromJarURL( state, baseName, SuffixType.Neither ) );
            if (library == null)
                library = createLibrary( state, tryResourceFromLoadPathOrURL( state, baseName, SuffixType.Neither ) );
            break;
        }

        return library;
    }

    @SuppressWarnings("rawtypes")
    protected LoadServiceResource tryResourceFromLoadPathOrURL(SearchState state, String baseName, SuffixType suffixType) {
         //System.err.println("tryResourceFromLoadPathOrUrl(" + baseName + "," +
         //suffixType + ")");
        LoadServiceResource foundResource = null;

        // if it's a ./ baseName, use CWD logic
        if (baseName.startsWith( "./" )) {
            foundResource = tryResourceFromCWD( state, baseName, suffixType );

            if (foundResource != null) {
                state.loadName = resolveLoadName( foundResource, foundResource.getName() );
            }

            // not found, don't bother with load path
            return foundResource;
        }

        // if it's a ~/ baseName use HOME logic
        if (baseName.startsWith( "~/" )) {
            foundResource = tryResourceFromHome( state, baseName, suffixType );

            if (foundResource != null) {
                state.loadName = resolveLoadName( foundResource, foundResource.getName() );
            }

            // not found, don't bother with load path
            return foundResource;
        }

        // if given path is absolute, just try it as-is (with extensions) and no
        // load path
        if (new File( baseName ).isAbsolute() || baseName.startsWith("../") || baseName.startsWith( "vfs:" )) {
            for (String suffix : suffixType.getSuffixes()) {
                String namePlusSuffix = baseName + suffix;
                foundResource = tryResourceAsIs( namePlusSuffix );

                if (foundResource != null) {
                    state.loadName = resolveLoadName( foundResource, namePlusSuffix );
                    return foundResource;
                }
            }

            return null;
        }

        Outer: for (int i = 0; i < loadPath.size(); i++) {
            // TODO this is really ineffient, and potentially a problem
            // everytime anyone require's something.
            // we should try to make LoadPath a special array object.
            RubyString entryString = loadPath.eltInternal( i ).convertToString();
            String loadPathEntry = entryString.asJavaString();

            if (loadPathEntry.equals( "." ) || loadPathEntry.equals( "" )) {
                foundResource = tryResourceFromCWD( state, baseName, suffixType );

                if (foundResource != null) {
                    String ss = foundResource.getName();
                    if(ss.startsWith( "./" )) {
                        ss = ss.substring( 2 );
                    }
                    state.loadName = resolveLoadName( foundResource, ss );
                    break Outer;
                }
            } else {
                boolean looksLikeJarURL = loadPathLooksLikeJarURL( loadPathEntry );
                for (String suffix : suffixType.getSuffixes()) {
                    String namePlusSuffix = baseName + suffix;

                    if (looksLikeJarURL) {
                        foundResource = tryResourceFromJarURLWithLoadPath( namePlusSuffix, loadPathEntry );
                    } else {
                        foundResource = tryResourceFromLoadPath( namePlusSuffix, loadPathEntry );
                    }

                    if (foundResource != null) {
                        String ss = namePlusSuffix;
                        if(ss.startsWith( "./" )) {
                            ss = ss.substring( 2 );
                        }
                        state.loadName = resolveLoadName( foundResource, ss );
                        break Outer; // end suffix iteration
                    }
                }
            }
        }

        return foundResource;
    }

    protected LoadServiceResource tryResourceFromLoadPath(String namePlusSuffix, String loadPathEntry) throws RaiseException {
         //System.err.println("tryResourceFromLoadPath(" + namePlusSuffix + ","
         //+ loadPathEntry + ")");
        LoadServiceResource foundResource = null;

        try {
            if (!Ruby.isSecurityRestricted()) {

                if (loadPathEntry.startsWith( "vfs:" )) {
                    try {
                        URL vfsUrl = makeUrl( loadPathEntry, namePlusSuffix );
                        VirtualFile file = VFS.getChild( vfsUrl.toURI() );
                        if (file != null && file.exists()) {
                            return unVFSifiedResource( file );
                            //return new NonLeakingLoadServiceResource( file.toURI().toURL(), vfsUrl.toExternalForm() );
                        }
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

                String reportedPath = loadPathEntry + "/" + namePlusSuffix;
                boolean absolute = true;
                // we check length == 0 for 'load', which does not use load path
                if (!new File( reportedPath ).isAbsolute()) {
                    absolute = false;
                    // prepend ./ if . is not already there, since we're loading based on CWD
                    if (reportedPath.charAt( 0 ) != '.') {
                        reportedPath = "./" + reportedPath;
                    }
                    loadPathEntry = JRubyFile.create( runtime.getCurrentDirectory(), loadPathEntry ).getAbsolutePath();
                }
                JRubyFile actualPath = JRubyFile.create( loadPathEntry, RubyFile.expandUserPath( runtime.getCurrentContext(), namePlusSuffix ) );
                if (RubyInstanceConfig.DEBUG_LOAD_SERVICE) {
                    debugLogTry( "resourceFromLoadPath", "'" + actualPath.toString() + "' " + actualPath.isFile() + " " + actualPath.canRead() );
                }
                if (actualPath.isFile() && actualPath.canRead()) {
                    foundResource = new LoadServiceResource( actualPath, reportedPath, absolute );
                    debugLogFound( foundResource );
                }
            }
        } catch (SecurityException secEx) {
        }

        return foundResource;
    }

    protected LoadServiceResource tryResourceAsIs(String namePlusSuffix) throws RaiseException {
         //System.err.println("tryResourceAsIs(" + namePlusSuffix + ")");
        LoadServiceResource foundResource = null;

        try {
            if (!Ruby.isSecurityRestricted()) {
                String reportedPath = namePlusSuffix;
                // we check length == 0 for 'load', which does not use load path
                if (reportedPath.startsWith( "vfs:" )) {
                    try {
                        URL vfsUrl = new URL( reportedPath );
                        // VirtualFile file = VFS.getRoot(vfsUrl);
                        VirtualFile file = VFS.getChild( vfsUrl.toURI() );
                        if (file != null && file.exists()) {
                            return unVFSifiedResource( file );
                            //return new NonLeakingLoadServiceResource( file.toURI().toURL(), reportedPath );
                        }
                    } catch (IOException e) {
                        // ignore
                    } catch (URISyntaxException e) {
                        // ignore
                    }
                    return null;
                }

                File actualPath = null;
                if (new File( reportedPath ).isAbsolute()) {
                    actualPath = new File( RubyFile.expandUserPath( runtime.getCurrentContext(), namePlusSuffix ) );
                } else {
                    // prepend ./ if . is not already there, since we're loading
                    // based on CWD
                    if (reportedPath.charAt( 0 ) == '.' && reportedPath.charAt( 1 ) == '/') {
                        reportedPath = reportedPath.replaceFirst( "\\./", runtime.getCurrentDirectory() );
                    }
                    actualPath = JRubyFile.create( runtime.getCurrentDirectory(), RubyFile.expandUserPath( runtime.getCurrentContext(), namePlusSuffix ) );
                }
                debugLogTry( "resourceAsIs", actualPath.toString() );
                if (actualPath.isFile() && actualPath.canRead()) {
                    try {
                        foundResource = new NonLeakingLoadServiceResource( actualPath.toURI().toURL(), reportedPath );
                        debugLogFound( foundResource );
                    } catch (MalformedURLException e) {
                        throw runtime.newIOErrorFromException( e );
                    }
                }
            }
        } catch (SecurityException secEx) {
            // ignore
        }

        return foundResource;
    }

    public void load(String file, boolean wrap) {
         //System.err.println("load(" + file + ")");
        if (!runtime.getProfile().allowLoad( file )) {
            throw runtime.newLoadError( "No such file to load -- " + file );
        }

        SearchState state = new SearchState( file );
        state.prepareLoadSearch( file );

        Library library = findBuiltinLibrary( state, state.searchFile, state.suffixType );
        if (library == null)
            library = findLibraryWithoutCWD( state, state.searchFile, state.suffixType );

        if (library == null) {
            library = findLibraryWithClassloaders( state, state.searchFile, state.suffixType );
            if (library == null) {
                throw runtime.newLoadError( "No such file to load -- " + file );
            }
        }
        try {
            library.load( runtime, wrap );
        } catch (IOException e) {
            if (runtime.getDebug().isTrue())
                e.printStackTrace( runtime.getErr() );
            throw runtime.newLoadError( "IO error -- " + file );
        }
    }

    protected LoadServiceResource tryResourceFromCWD(SearchState state, String baseName, SuffixType suffixType) throws RaiseException {
        LoadServiceResource foundResource = null;

        for (String suffix : suffixType.getSuffixes()) {
            String namePlusSuffix = baseName + suffix;
            // check current directory; if file exists, retrieve URL and return
            // resource
            try {
                JRubyFile file = JRubyFile.create( runtime.getCurrentDirectory(), RubyFile.expandUserPath( runtime.getCurrentContext(), namePlusSuffix ) );
                debugLogTry( "resourceFromCWD", file.toString() );
                if (file.isFile() && file.isAbsolute() && file.canRead()) {
                    boolean absolute = true;
                    String s = namePlusSuffix;
                    if (!namePlusSuffix.startsWith( "./" )) {
                        s = "./" + s;
                    }
                    foundResource = new NonLeakingLoadServiceResource( file.toURI().toURL(), s, absolute );
                    debugLogFound( foundResource );
                    state.loadName = resolveLoadName( foundResource, namePlusSuffix );
                    break;
                }
            } catch (IllegalArgumentException illArgEx) {
            } catch (SecurityException secEx) {
            } catch (IOException ioEx) {
            }
        }

        return foundResource;
    }

    protected NonLeakingLoadServiceResource unVFSifiedResource(VirtualFile vFile) throws IOException {
        File file = vFile.getPhysicalFile();
        return new NonLeakingLoadServiceResource( file.toURI().toURL(), file.getAbsolutePath() );
    }
    
    /*
     * @Override protected void addLoadedFeature(RubyString loadNameRubyString)
     * { System.err.println( "addLoadedFeature(" + loadNameRubyString + ")" );
     * super.addLoadedFeature(loadNameRubyString); }
     * 
     * @Override protected boolean featureAlreadyLoaded(RubyString
     * loadNameRubyString) { boolean result =
     * super.featureAlreadyLoaded(loadNameRubyString); System.err.println(
     * "featureAlreadyLoaded(" + loadNameRubyString + ") " + result ); return
     * result; }
     */

}
