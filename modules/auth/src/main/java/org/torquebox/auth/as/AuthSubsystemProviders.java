package org.torquebox.auth.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

public class AuthSubsystemProviders {

    static final String RESOURCE_NAME = AuthSubsystemProviders.class.getPackage().getName() + ".LocalDescriptions";

    public static final DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            final ResourceBundle bundle = getResourceBundle( locale );

            final ModelNode subsystem = new ModelNode();
            subsystem.get( DESCRIPTION ).set( bundle.getString( "torquebox-auth" ) );
            subsystem.get( HEAD_COMMENT_ALLOWED ).set( true );
            subsystem.get( TAIL_COMMENT_ALLOWED ).set( true );
            subsystem.get( NAMESPACE ).set( Namespace.CURRENT.getUriString() );

            return subsystem;
        }
    };

    static final DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            final ResourceBundle bundle = getResourceBundle( locale );
            final ModelNode operation = new ModelNode();

            operation.get( OPERATION_NAME ).set( ADD );
            operation.get( DESCRIPTION ).set( bundle.getString( "torquebox-auth.add" ) );
            operation.get( REQUEST_PROPERTIES ).setEmptyObject();
            operation.get( REPLY_PROPERTIES ).setEmptyObject();

            return operation;
        }
    };

    private static ResourceBundle getResourceBundle(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return ResourceBundle.getBundle( RESOURCE_NAME, locale );
    }
}
