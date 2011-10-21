package org.torquebox.core.processors;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.processors.AbstractParsingProcessor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Base abstract class for anything that parses YAML.
 * 
 * @author Mike Dobozy
 */
public abstract class AbstractYamlParsingProcessor extends AbstractParsingProcessor {

    @Override
    public abstract void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException;

    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseYaml(VirtualFile file) throws DeploymentUnitProcessingException {
        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load( in );
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return data;
        } catch (YAMLException e) {
            throw new DeploymentUnitProcessingException( "Error parsing YAML: ", e );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( "Error reading YAML: ", e );
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
    }

}
