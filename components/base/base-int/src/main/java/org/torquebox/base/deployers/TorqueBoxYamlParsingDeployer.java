package org.torquebox.base.deployers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.yaml.snakeyaml.Yaml;

public class TorqueBoxYamlParsingDeployer extends AbstractParsingDeployer {

    public TorqueBoxYamlParsingDeployer() {
        addOutput(TorqueBoxMetaData.class);
        setStage(DeploymentStages.PARSE);
    }

    @Override
    protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        VirtualFile file = getMetaDataFile(unit, "torquebox.yml");

        if (file == null) {
            return;
        }

        try {
            TorqueBoxMetaData metaData = parse(unit, file);
            unit.addAttachment(TorqueBoxMetaData.class, metaData);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    protected TorqueBoxMetaData parse(VFSDeploymentUnit unit, VirtualFile file) throws Exception {
        TorqueBoxMetaData externalMetaData = unit.getAttachment(TorqueBoxMetaData.EXTERNAL, TorqueBoxMetaData.class);
        TorqueBoxMetaData internalMetaData = parse(file);

        if (externalMetaData == null) {
            return internalMetaData;
        }

        return externalMetaData.overlayOnto(internalMetaData);
    }

    @SuppressWarnings("unchecked")
    static TorqueBoxMetaData parse(VirtualFile file) throws IOException {

        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load(in);
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return new TorqueBoxMetaData(data);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
