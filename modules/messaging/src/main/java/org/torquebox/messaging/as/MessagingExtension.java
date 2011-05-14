package org.torquebox.messaging.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;

public class MessagingExtension implements Extension {

    @Override
    public void initialize(ExtensionContext context) {
        log.info( "Initializing TorqueBox Messaging Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( MessagingSubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                MessagingSubsystemAdd.ADD_INSTANCE,
                MessagingSubsystemProviders.SUBSYSTEM_ADD,
                false );
        
        registration.registerXMLElementWriter(MessagingSubsystemParser.getInstance());

        log.info( "Initialized TorqueBox Messaging Subsystem" );
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(Namespace.CURRENT.getUriString(), MessagingSubsystemParser.getInstance());
    }
    
    
    public static final String SUBSYSTEM_NAME = "torquebox-messaging";
    static final Logger log = Logger.getLogger( "org.torquebox.messaging.as" );


}
