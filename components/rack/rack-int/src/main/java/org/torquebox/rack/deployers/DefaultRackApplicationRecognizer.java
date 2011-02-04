package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractRecognizer;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class DefaultRackApplicationRecognizer extends AbstractRecognizer {
    
    public static final String DEFAULT_RACKUP_PATH = "config.ru";
    
    public DefaultRackApplicationRecognizer() {
        addInput(RackApplicationMetaData.class);
        addOutput(RackApplicationMetaData.class);
        setRelativeOrder( 5000 );
    }

    @Override
    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return hasAnyOf(unit.getRoot(), DEFAULT_RACKUP_PATH );
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        log.info("Recognized rack application: " + unit);
        RackApplicationMetaData rackAppMetaData = unit.getAttachment(RackApplicationMetaData.class);

        if (rackAppMetaData == null) {
            log.info("Initializing rack application: " + unit);
            rackAppMetaData = new RackApplicationMetaData();
            rackAppMetaData.setRackUpScriptLocation( DEFAULT_RACKUP_PATH );
            unit.addAttachment(RackApplicationMetaData.class, rackAppMetaData);
        }
    }

}
