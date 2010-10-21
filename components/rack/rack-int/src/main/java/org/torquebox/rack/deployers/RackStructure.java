package org.torquebox.rack.deployers;

import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.torquebox.mc.vdf.AbstractRubyStructureDeployer;
import org.jboss.vfs.VirtualFile;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class RackStructure extends AbstractRubyStructureDeployer {

    public RackStructure() {
        // We should come after RailsStructure, since Rails 3 apps
        // include a config.ru, which we would mistake for pure rack
        setRelativeOrder( -999 );
    }

    @Override
    protected boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {
        VirtualFile file = structureContext.getFile();
        ContextInfo context = null;
        try {
            VirtualFile rackup = file.getChild("config.ru");
            if (rackup != null && rackup.exists()) {
                context = createContext(structureContext, new String[] { "config" });
                addDirectoryOfJarsToClasspath(structureContext, context, "lib/java");
                addRackApplicationMetaData( structureContext, context );
                return true;
            }
        } catch (IOException e) {
            if (context != null) structureContext.removeChild(context);
            throw new DeploymentException(e);
        }
        return false;
    }

    protected void addRackApplicationMetaData(StructureContext structureContext, ContextInfo context) throws IOException {
        MutableAttachments attachments = (MutableAttachments) context.getPredeterminedManagedObjects();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        rackAppMetaData.setRackRoot( structureContext.getRoot() );
        attachments.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
    }

    @Override
    protected boolean hasValidSuffix(String name) {
        return name.endsWith(".rack");
    }

    @Override
    protected boolean hasValidName(VirtualFile file) {
        return hasValidSuffix( file.getName() );
    }

}
