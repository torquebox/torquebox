package org.torquebox.rails.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractRecognizer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

public class RailsApplicationRecognizer extends AbstractRecognizer {

    public RailsApplicationRecognizer() {
        addInput( RailsApplicationMetaData.class );
        addInput( RackApplicationMetaData.class );
        addOutput( RailsApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
        setRelativeOrder( 1000 );
    }

    @Override
    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return hasAnyOf( unit.getRoot(), "config/boot.rb" );
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        log.info( "Recognized rails application: " + unit );
        RailsApplicationMetaData railsAppMetaData = unit.getAttachment( RailsApplicationMetaData.class );

        if (railsAppMetaData == null) {
            log.info( "Initializing rails application: " + unit );
            railsAppMetaData = new RailsApplicationMetaData();
            unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
        }
    }

}
