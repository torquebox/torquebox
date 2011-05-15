package org.torquebox.cdi.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;

public class CDIExtension implements Extension {

    @Override
    public void initialize(ExtensionContext context) {
        log.info( "Initializing TorqueBox CDI Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( CDISubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                CDISubsystemAdd.ADD_INSTANCE,
                CDISubsystemProviders.SUBSYSTEM_ADD,
                false );
        
        registration.registerXMLElementWriter(CDISubsystemParser.getInstance());
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(Namespace.CURRENT.getUriString(), CDISubsystemParser.getInstance());
    }
    
    
    public static final String SUBSYSTEM_NAME = "torquebox-cdi";
    static final Logger log = Logger.getLogger( "org.torquebox.cdi.as" );


}
