package org.torquebox.base.deployers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.yaml.snakeyaml.Yaml;

public class TorqueBoxYamlParsingDeployer extends AbstractVFSParsingDeployer<TorqueBoxMetaData> {

    public TorqueBoxYamlParsingDeployer() {
        super(TorqueBoxMetaData.class);
        setName("torquebox.yml");
    }

    @Override
    protected TorqueBoxMetaData parse(VFSDeploymentUnit unit, VirtualFile file, TorqueBoxMetaData root) throws Exception {
        return parse(file);
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
