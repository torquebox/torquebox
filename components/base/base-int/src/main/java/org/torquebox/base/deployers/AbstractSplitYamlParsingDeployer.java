package org.torquebox.base.deployers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.yaml.snakeyaml.Yaml;

/** Abstract deployer base-class supporting <code>torquebox.yml</code> sectional parsing.
 * 
 * <p>For a given subsystem 'foo', a torquebox.yml section named 'foo:' can configure it
 * or optionally (deprecated) a file named foo.yml.</p>
 * 
 * @author Bob McWhirter
 */
public abstract class AbstractSplitYamlParsingDeployer extends AbstractDeployer {

    /** Name of the section within torquebox.yml. */
    private String sectionName;
    
    /** Opotional fine-name for NAME.yml parsing separate from torquebox.yml. */
    private String fileName;

    public AbstractSplitYamlParsingDeployer() {
        addInput(TorqueBoxMetaData.class);
        setStage(DeploymentStages.PARSE);
    }

    public String getSectionName() {
        return this.sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getFileName() {
        if (this.fileName != null) {
            return this.fileName;
        }

        return getSectionName() + ".yml";
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (!(unit instanceof VFSDeploymentUnit)) {
            throw new DeploymentException("Only supports VFS deployments");
        }

        deploy((VFSDeploymentUnit) unit);
    }

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        TorqueBoxMetaData globalMetaData = unit.getAttachment(TorqueBoxMetaData.class);

        Map<String, ?> data = null;

        if (globalMetaData != null) {
            data = globalMetaData.getSection(getSectionName());
        }

        if (data == null) {
            VirtualFile metaDataFile = unit.getMetaDataFile(getFileName());
            if (metaDataFile.exists()) {
                InputStream in = null;
                try {
                    in = metaDataFile.openStream();
                    Yaml yaml = new Yaml();
                    data = (Map<String, ?>) yaml.load(in);
                } catch (IOException e) {
                    throw new DeploymentException( e );
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            throw new DeploymentException( e );
                        }
                    }
                }
            }
        }
        
        if ( data == null ) {
            return;
        }
        
        parse( unit, data );
    }
    
    public abstract void parse(VFSDeploymentUnit unit, Map<String,?> data) throws DeploymentException;

}
