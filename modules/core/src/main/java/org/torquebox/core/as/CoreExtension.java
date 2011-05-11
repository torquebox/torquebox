package org.torquebox.core.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;

public class CoreExtension implements Extension {

    @Override
    public void initialize(ExtensionContext context) {
        log.info( "Initializing TorqueBox Core Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( CoreSubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                CoreSubsystemAdd.ADD_INSTANCE,
                CoreSubsystemProviders.SUBSYSTEM_ADD,
                false );
        
        registration.registerXMLElementWriter(CoreSubsystemParser.getInstance());

        log.info( "Initialized TorqueBox Core Subsystem" );
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(Namespace.CURRENT.getUriString(), CoreSubsystemParser.getInstance());
    }
    
    
    public static final String SUBSYSTEM_NAME = "torquebox-core";
    static final Logger log = Logger.getLogger( "org.torquebox.core.as" );


}
