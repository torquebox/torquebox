package org.torquebox.injection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

public class AnalyzingRubyProxyInjectionBuilder extends BaseRubyProxyInjectionBuilder {

    public AnalyzingRubyProxyInjectionBuilder(DeploymentUnit context, BeanMetaDataBuilder beanBuilder, InjectionAnalyzer analyzer, RubyRuntimeMetaData.Version rubyVersion) {
        super( context, beanBuilder );
        this.analyzer = analyzer;
        this.rubyVersion = rubyVersion;
    }
    
    public void analyzeAndInject(String path) throws URISyntaxException, IOException {
        VirtualFile source = locateSource( path );
        if ( source == null ) {
            return;
        }
        
        List<Injectable> injectables = this.analyzer.analyze( source, this.rubyVersion );
        injectInjectionRegistry(injectables );
    }
    
    public void analyzeRecursivelyAndInject(String propertyName, VirtualFile root) throws URISyntaxException, IOException {
        List<Injectable> injectables = this.analyzer.analyzeRecursively( root, this.rubyVersion );
        injectInjectionRegistry( injectables );
    }
    
    protected List<URL> getLoadPathURLs() {
        RubyRuntimeMetaData runtimeMetaData = getContext().getAttachment( RubyRuntimeMetaData.class );
        List<URL> urls = new ArrayList<URL>();

        if (runtimeMetaData != null) {
            List<RubyLoadPathMetaData> loadPaths = runtimeMetaData.getLoadPaths();
            for (RubyLoadPathMetaData path : loadPaths) {
                urls.add( path.getURL() );
            }
        }

        return urls;
    }

    protected VirtualFile locateSource(String path) throws MalformedURLException, URISyntaxException {
        List<URL> loadPathURLs = getLoadPathURLs();

        for (URL each : loadPathURLs) {
            if (each.getProtocol().equals( "vfs" )) {
                URL candidateUrl = new URL( each, path );
                VirtualFile candidateFile = VFS.getChild( candidateUrl.toURI() );

                if (candidateFile.exists()) {
                    return candidateFile;
                }
            }
        }

        return null;
    }

    private InjectionAnalyzer analyzer;
    private RubyRuntimeMetaData.Version rubyVersion;

}
