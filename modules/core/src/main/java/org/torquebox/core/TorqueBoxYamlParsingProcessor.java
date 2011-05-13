package org.torquebox.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.yaml.snakeyaml.Yaml;

public class TorqueBoxYamlParsingProcessor extends AbstractParsingProcessor {
    public static final String TORQUEBOX_YAML_FILE = "torquebox.yml";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        VirtualFile file = getMetaDataFile( root, TORQUEBOX_YAML_FILE );
        
        if ( file != null ) {
            try {
                TorqueBoxMetaData metaData = parse( file );
                unit.putAttachment( TorqueBoxMetaData.ATTACHMENT_KEY, metaData );
            } catch (IOException e) {
                throw new DeploymentUnitProcessingException( e );
            }
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static TorqueBoxMetaData parse(VirtualFile file) throws IOException {
        log.info( "parsing: " + file );

        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load( in );
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return new TorqueBoxMetaData( data );
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core" );

}
