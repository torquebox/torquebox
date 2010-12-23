
package org.torquebox.rack.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.yaml.snakeyaml.Yaml;


public class TorqueBoxYamlParser {
    
    public TorqueBoxYamlParser() {
    }

    public TorqueBoxYamlParser(RackApplicationMetaData metaData) {
        this.metaData = metaData;
    }

    public TorqueBoxYamlParser(RackApplicationMetaData metaData, VirtualFile root) {
        this.metaData = metaData;
        getMetaData().setRackRoot( root );
    }

    public RackApplicationMetaData parse(VirtualFile file) throws IOException {
        InputStream in = file.openStream();
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> config = (Map<String, Object>) yaml.load(file.openStream());
            return config==null ? null : parse(config);
        } finally {
            in.close();
        }
    }

    public RackApplicationMetaData parse(Map<String,Object> config) throws IOException {
        parseApplication( (Map<String,String>) config.get( "application" ) );
        parseWeb( (Map<String,Object>) config.get( "web" ) );
        parseEnvironment( (Map<String,String>) config.get( "environment" ) );
        return getMetaData();
    }

    public RackApplicationMetaData parseApplication(Map<String,String> application) throws IOException {
        if (application != null) {
            getMetaData().setRackRoot( getOneOf( application, "RACK_ROOT", "RAILS_ROOT", "root" ) );
            getMetaData().setRackEnv( getOneOf( application, "RACK_ENV", "RAILS_ENV", "env" ) );
            getMetaData().setRackUpScriptLocation( application.get( "rackup" ) );
        }
        return getMetaData();
    }

    public RackApplicationMetaData parseWeb(Map<String,Object> web) {
        if (web != null) {
            getMetaData().setContextPath( (String) web.get( "context" ) );
            getMetaData().setStaticPathPrefix( (String) web.get( "static" ) );
            parseHosts( web.get( "host" ) );
        }
        return getMetaData();
    }

    public RackApplicationMetaData parseEnvironment(Map<String,String> environment) {
        if (environment != null) {
            getMetaData().setEnvironmentVariables( environment );
        }
        return getMetaData();
    }

    public RackApplicationMetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new WriteOnceRackApplicationMetaData();
        }
        return this.metaData;
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

    protected String getOneOf(Map<String,String> map, String... keys) {
        for (String each: keys) {
            for (String key: map.keySet()) {
                if (each.equalsIgnoreCase(key)) {
                    return map.get(key);
                }
            }
        }
        return null;
    }

    private RackApplicationMetaData metaData;
    
}