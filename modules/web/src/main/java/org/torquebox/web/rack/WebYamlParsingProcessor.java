package org.torquebox.web.rack;

import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;

public class WebYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {
    
    public WebYamlParsingProcessor() {
        setSectionName( "web" );
        setSupportsStandalone( false );
    }
    
    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        
        log.info( "parsing: " + dataObj );
        
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            rackAppMetaData = new RackApplicationMetaData();
            unit.putAttachment( RackApplicationMetaData.ATTACHMENT_KEY, rackAppMetaData );
        }
        
        Map<String, Object> webData = (Map<String, Object>) dataObj;
        
        rackAppMetaData.setContextPath( (String) webData.get( "context" ) );
        rackAppMetaData.setStaticPathPrefix( (String) webData.get( "static" ) );
        
        if (webData.get( "rackup" ) != null ) {
            rackAppMetaData.setRackUpScriptLocation( (String) webData.get( "rackup" ) );
        }
        
        Object hosts = webData.get( "host" );

        if (hosts instanceof List) {
            List<String> list = (List<String>) hosts;
            for (String each : list) {
                rackAppMetaData.addHost( each );
            }
        } else {
            rackAppMetaData.addHost( (String) hosts );
        }
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );

}
