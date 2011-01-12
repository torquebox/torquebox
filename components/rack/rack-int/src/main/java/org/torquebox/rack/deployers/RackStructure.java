package org.torquebox.rack.deployers;

import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.torquebox.mc.vdf.AbstractRubyStructureDeployer;
import org.jboss.vfs.VirtualFile;


/**
 * <pre>
 * Stage: structure
 *    In: 
 *   Out: classpath entries and metadata locations
 * </pre>
 *
 * Determine structure for Rack apps
 */
public class RackStructure extends AbstractRubyStructureDeployer {

    public RackStructure() {
        // We should come after RailsStructure, since Rails 3 apps
        // include a config.ru, which we would mistake for pure rack
        setRelativeOrder( -999 );
    }

    @Override
    protected boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {
        VirtualFile root = structureContext.getFile();
        ContextInfo context = null;
        try {
            if (hasConfigRu(root) || hasTorqueboxYml(root)) {
                log.info("Identified as Rack app: " + root);
                context = createContext(structureContext, new String[] { "", "config" });
                log.info("Adding lib/ to classpath" );
                addDirectoryOfJarsToClasspath(structureContext, context, "lib");
                addDirectoryOfJarsToClasspath(structureContext, context, "vendor/jars");
                return true;
            }
        } catch (IOException e) {
            if (context != null) structureContext.removeChild(context);
            throw new DeploymentException(e);
        }
        return false;
    }

    private boolean hasConfigRu(VirtualFile root) {
        return root.getChild("config.ru").exists();
    }

    private boolean hasTorqueboxYml(VirtualFile root) {
        return root.getChild("torquebox.yml").exists();
    }

	@Override
	protected boolean hasValidName(VirtualFile file) {
		return file.getName().endsWith( ".rack" );
	}

	@Override
	protected boolean hasValidSuffix(String name) {
		return true;
	}

}