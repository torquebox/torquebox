/* Copyright 2009 Red Hat, Inc. */

package org.torquebox.interp.deployers;

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Parsing deployer for {@code ruby.yml} to specify ruby 1.8 -vs- 1.9, at least.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RubyYamlParsingDeployer extends AbstractDeployer {

    /**
     * Construct.
     */
    public RubyYamlParsingDeployer() {
        setInput(RubyRuntimeMetaData.class);
    }
    
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy((VFSDeploymentUnit) unit);
        }
    }
    
    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        VirtualFile file = unit.getMetaDataFile("ruby.yml");

        if (file != null) {
            try {
                RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(RubyRuntimeMetaData.class);
                if (runtimeMetaData != null) {
                    parse(unit, file, runtimeMetaData);
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void parse(VFSDeploymentUnit unit, VirtualFile file, RubyRuntimeMetaData runtimeMetaData) throws Exception {
        
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> config = (Map<String, Object>) yaml.load(file.openStream());

            if (config != null) {
                Object version = config.get("version");
                if ("1.8".equals("" + version)) {
                    runtimeMetaData.setVersion(RubyRuntimeMetaData.Version.V1_8);
                } else if ("1.9".equals("" + version)) {
                    runtimeMetaData.setVersion(RubyRuntimeMetaData.Version.V1_9);
                }
            }
        } catch (YAMLException e) {
            log.error("Error parsing ruby.yml: " + e.getMessage());
        }
    }


}
