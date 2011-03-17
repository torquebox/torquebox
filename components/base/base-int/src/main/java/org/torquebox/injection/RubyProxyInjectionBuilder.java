package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

public class RubyProxyInjectionBuilder {
    
    private static final Logger log = Logger.getLogger(  RubyProxyInjectionBuilder.class );

    private DeploymentUnit context;
    private InjectionAnalyzer injectionAnalyzer;
    private BeanMetaDataBuilder beanBuilder;

    public RubyProxyInjectionBuilder(DeploymentUnit context, InjectionAnalyzer injectionAnalyzer, BeanMetaDataBuilder beanBuilder) {
        this.context = context;
        this.injectionAnalyzer = injectionAnalyzer;
        this.beanBuilder = beanBuilder;
    }

    protected List<URL> getLoadPathURLs() {
        RubyRuntimeMetaData runtimeMetaData = this.context.getAttachment( RubyRuntimeMetaData.class );
        List<URL> urls = new ArrayList<URL>();

        if (runtimeMetaData != null) {
            List<RubyLoadPathMetaData> loadPaths = runtimeMetaData.getLoadPaths();
            for (RubyLoadPathMetaData path : loadPaths) {
                urls.add( path.getURL() );
            }
        }

        return urls;
    }

    public void build(Collection<Injection> injections) {
        addInjections( injections );
    }

    public void build(InputStream source) throws IOException {
        List<Injection> injections = this.injectionAnalyzer.analyze( source );
        if (injections == null || injections.isEmpty()) {
            return;
        }

        addInjections( injections );
    }

    public void build(VirtualFile source) throws IOException {
        InputStream in = null;

        try {
            in = source.openStream();
            build( in );
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void build(String sourceRelativePath) throws IOException, URISyntaxException {
        VirtualFile source = locateSource( sourceRelativePath );
        
        if ( source != null ) {
            build( source );
        }
    }

    protected VirtualFile locateSource(String path) throws MalformedURLException, URISyntaxException {
        List<URL> loadPathURLs = getLoadPathURLs();

        for (URL each : loadPathURLs) {
            if (each.getProtocol().equals( "vfs" )) {
                URL candidateUrl = new URL( each, path );
                VirtualFile candidateFile = VFS.getChild( candidateUrl.toURI() );
                
                if ( candidateFile.exists() ) {
                    return candidateFile;
                }
            }
        }
        
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void addInjections(Collection<Injection> injections) {
        Map injectionMap = beanBuilder.createMap();
        beanBuilder.addPropertyMetaData( "rubyProxyInjectionMap", injectionMap );

        for (Injection injection : injections) {
            ValueMetaData siteNameKey = beanBuilder.createString( String.class.getName(), injection.getSiteName() );
            injectionMap.put( siteNameKey, injection.getInjectable().createMicrocontainerInjection( context, beanBuilder ) );
        }
    }
}
