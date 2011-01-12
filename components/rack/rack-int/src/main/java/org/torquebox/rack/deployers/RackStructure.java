package org.torquebox.rack.deployers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.vdf.AbstractRubyStructureDeployer;


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
    
    public static ContextInfo createRackContextInfo(VirtualFile rackRoot, StructureMetaData structureMetaData) throws IOException {
        List<String> metaDataPaths = new ArrayList<String>();
        metaDataPaths.add("");
        metaDataPaths.add("config");
        
        List<ClassPathEntry> classPaths = getClassPathEntries( rackRoot.getChild( "lib" ), rackRoot );
        classPaths.addAll( getClassPathEntries( rackRoot.getChild( "vendor/jars" ), rackRoot ) );
        
        ContextInfo context = StructureMetaDataFactory.createContextInfo("", metaDataPaths, classPaths);
        return context;
    }

    @Override
    protected boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {
        VirtualFile root = structureContext.getFile();
        try {
            if (hasConfigRu(root) || hasTorqueboxYml(root)) {
                StructureMetaData structureMetaData = structureContext.getMetaData();
                ContextInfo context = createRackContextInfo(root, structureMetaData);
                structureMetaData.addContext( context );
                return true;
            }
        } catch (IOException e) {
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