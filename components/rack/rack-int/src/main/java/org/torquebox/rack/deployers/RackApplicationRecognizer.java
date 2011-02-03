package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractRecognizer;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class RackApplicationRecognizer extends AbstractRecognizer {

    @Override
    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return hasAnyOf(unit.getRoot(), "torquebox.yml", "config/torquebox.yml", "config.ru", "Rakefile", ".bundle/config");

    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        log.info("Recognized rack application: " + unit);
        RackApplicationMetaData rackAppMetaData = unit.getAttachment(RackApplicationMetaData.class);

        if (rackAppMetaData == null) {
            log.info("Initializing rack application: " + unit);
            rackAppMetaData = new RackApplicationMetaData();
            unit.addAttachment(RackApplicationMetaData.class, rackAppMetaData);
        }
    }

}
