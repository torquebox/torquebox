
package org.torquebox.rack.metadata;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.yaml.snakeyaml.Yaml;


public class TorqueBoxYamlParser {
    
    public TorqueBoxYamlParser() {
    }

    public TorqueBoxYamlParser(RackApplicationMetaData metaData) {
        this.metaData = metaData;
    }

    public RackApplicationMetaData parse(VirtualFile file) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> config = (Map<String, Object>) yaml.load(file.openStream());
        return parse(config);
    }

    public RackApplicationMetaData parse(Map<String,Object> config) throws IOException {
        parseApplication( (Map<String,String>) config.get( "application" ) );
        parseWeb( (Map<String,Object>) config.get( "web" ) );
        parseEnvironment( (Map<String,String>) config.get( "environment" ) );
        return getMetaData();
    }

    public RackApplicationMetaData parseApplication(Map<String,String> application) throws IOException {
        getMetaData().setRackRoot( VFS.getChild( application.get( "RACK_ROOT" ) ) );
        getMetaData().setRackEnv( application.get( "RACK_ENV" ) );
        getMetaData().setRackUpScript( getAbsoluteOrRelativeFile( application.get( "rackup" ) ) );
        return getMetaData();
    }

    public RackApplicationMetaData parseWeb(Map<String,Object> web) {
        getMetaData().setContextPath( (String) web.get( "context" ) );
        getMetaData().setStaticPathPrefix( (String) web.get( "static" ) );
        parseHosts( web.get( "host" ) );
        return getMetaData();
    }

    public RackApplicationMetaData parseEnvironment(Map<String,String> environment) {
        getMetaData().setEnvironmentVariables( environment );
        return getMetaData();
    }

    public RackApplicationMetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new RackApplicationMetaData();
        }
        return this.metaData;
    }

    protected VirtualFile getAbsoluteOrRelativeFile(String path) {
        return (path.startsWith("/") || path.matches( "^[A-Za-z]:.*") ) ? VFS.getChild(path) : getMetaData().getRackRoot().getChild(path);
    }

    protected RackApplicationMetaData parseHosts(Object hosts) {
        if (hosts instanceof List) {
            List<String> list = (List<String>) hosts;
            for (String each: list) {
                getMetaData().addHost(each);
            }
        } else {
            getMetaData().addHost( (String) hosts );
        }
        return getMetaData();
    }

    private RackApplicationMetaData metaData;
    
}