package org.torquebox.core.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;

public class CoreExtension extends AbstractBootstrappableExtension {

    @Override
    public void initialize(ExtensionContext context) {

        bootstrap();
        
        log.info( "Initializing TorqueBox Core Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( CoreSubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                CoreSubsystemAdd.ADD_INSTANCE,
                CoreSubsystemProviders.SUBSYSTEM_ADD,
                false );

        ModelNodeRegistration injector = subsystem.registerSubModel( PathElement.pathElement( "injector" ), CoreSubsystemProviders.INJECTOR );

        injector.registerOperationHandler( "add",
                InjectableHandlerAdd.ADD_INSTANCE,
                CoreSubsystemProviders.INJECTOR_ADD,
                false );

        registration.registerXMLElementWriter( CoreSubsystemParser.getInstance() );

    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping( Namespace.CURRENT.getUriString(), CoreSubsystemParser.getInstance() );
    }

    public static final String SUBSYSTEM_NAME = "torquebox-core";
    static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
