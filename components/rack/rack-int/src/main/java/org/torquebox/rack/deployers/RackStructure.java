package org.torquebox.rack.deployers;

import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.torquebox.mc.vdf.AbstractRubyStructureDeployer;

public class RackStructure extends AbstractRubyStructureDeployer {

	@Override
	protected boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {

		ContextInfo context = createContext(structureContext, new String[] { "config" });

		try {
			addDirectoryOfJarsToClasspath(structureContext, context, "lib/java");
		} catch (IOException e) {
			structureContext.removeChild(context);
			throw new DeploymentException(e);
		}
		
		return true;

	}

	@Override
	protected boolean hasValidSuffix(String name) {
		return name.endsWith(".rack");
	}

}
