package org.torquebox.auth.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;

public class AuthExtension implements Extension {

    @Override
    public void initialize(ExtensionContext context) {
        log.info( "Initializing TorqueBox Auth Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( AuthSubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                AuthSubsystemAdd.ADD_INSTANCE,
                AuthSubsystemProviders.SUBSYSTEM_ADD,
                false );

        registration.registerXMLElementWriter( AuthSubsystemParser.getInstance() );

        log.info( "Initialized TorqueBox Auth Subsystem" );
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping( Namespace.CURRENT.getUriString(), AuthSubsystemParser.getInstance() );
    }

    public static final String SUBSYSTEM_NAME = "torquebox-auth";
    static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );
}
