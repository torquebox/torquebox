package org.torquebox.injection;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.injection.spi.InjectableRegistry;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.mc.AttachmentUtils;

public class RubyProxyInjectionBuilder {

    private static final Logger log = Logger.getLogger( RubyProxyInjectionBuilder.class );

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

    public void build(Collection<Injectable> injectables) {
        addInjectableRegistry( injectables );
    }

    public void build(InputStream source) throws IOException {
        List<Injectable> injectables = this.injectionAnalyzer.analyze( source );
        if (injectables == null || injectables.isEmpty()) {
            return;
        }

        addInjectableRegistry( injectables );
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

        if (source != null) {
            build( source );
        }
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

    protected void addInjectableRegistry(Collection<Injectable> injectables) {

        Map<String, Collection<Injectable>> collated = collate( injectables );

        List<ValueMetaData> collections = beanBuilder.createList();
        
        BeanMetaDataBuilder registryBuilder = BeanMetaDataBuilder.createBuilder( beanBuilder.getBeanMetaData().getName() + "$" + InjectableRegistry.class.getName(), InjectableRegistryImpl.class.getName() );

        for (String collectionName : collated.keySet()) {
            Collection<Injectable> collectionInjectables = collated.get( collectionName );

            String collectionBeanName = beanBuilder.getBeanMetaData().getName() + "$" + InjectableCollection.class.getName() + "$" + collectionName;
            BeanMetaDataBuilder collectionBuilder = BeanMetaDataBuilder.createBuilder( collectionBeanName, InjectableCollection.class.getName() );
            collectionBuilder.addConstructorParameter( "java.lang.String", collectionName );
            Map<ValueMetaData, ValueMetaData> collectionMap = collectionBuilder.createMap();
            for (Injectable each : collectionInjectables) {
                ValueMetaData collectionKey = collectionBuilder.createString( String.class.getName(), each.getKey() );
                collectionMap.put( collectionKey, each.createMicrocontainerInjection( this.context, collectionBuilder ) );
            }
            collectionBuilder.addConstructorParameter( "java.util.Map", collectionMap );
            collections.add( collectionBuilder.getBeanMetaData() );
        }

        registryBuilder.addPropertyMetaData( "collections", collections );

        beanBuilder.addPropertyMetaData( "injectableRegistry", registryBuilder.getBeanMetaData() );
    }

    protected Map<String, Collection<Injectable>> collate(Collection<Injectable> injectables) {
        Map<String, Collection<Injectable>> collated = new HashMap<String, Collection<Injectable>>();

        for (Injectable each : injectables) {
            Collection<Injectable> collection = collated.get( each.getType() );
            if (collection == null) {
                collection = new ArrayList<Injectable>();
                collated.put( each.getType(), collection );
            }
            collection.add( each );
        }

        return collated;
    }
}
