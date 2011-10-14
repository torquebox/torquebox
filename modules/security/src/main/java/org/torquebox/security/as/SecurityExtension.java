/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.security.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.as.AbstractBootstrappableExtension;
import org.torquebox.core.as.GenericSubsystemDescribeHandler;
import org.torquebox.security.auth.as.AuthSubsystemAdd;
import org.torquebox.security.auth.as.AuthSubsystemParser;
import org.torquebox.security.auth.as.AuthSubsystemProviders;

public class SecurityExtension extends AbstractBootstrappableExtension {

    @Override
    public void initialize(ExtensionContext context) {
        bootstrap();
        initializeAuthentication(context);
    }

	private void initializeAuthentication(ExtensionContext context) {
		log.info( "Initializing TorqueBox Auth Subsystem" );
        final SubsystemRegistration registration = context.registerSubsystem( AUTHENTICATION_SUBSYSTEM_NAME );
        final ManagementResourceRegistration subsystem = registration.registerSubsystemModel( AuthSubsystemProviders.SUBSYSTEM );

        subsystem.registerOperationHandler( ADD,
                AuthSubsystemAdd.ADD_INSTANCE,
                AuthSubsystemProviders.SUBSYSTEM_ADD,
                false );
        
        subsystem.registerOperationHandler(DESCRIBE, 
                GenericSubsystemDescribeHandler.INSTANCE, 
                GenericSubsystemDescribeHandler.INSTANCE, 
                false, 
                OperationEntry.EntryType.PRIVATE);

        registration.registerXMLElementWriter( AuthSubsystemParser.getInstance() );
	}

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping( Namespace.CURRENT.getUriString(), AuthSubsystemParser.getInstance() );
    }

    public static final String AUTHENTICATION_SUBSYSTEM_NAME = "torquebox-auth";
    static final Logger log = Logger.getLogger( "org.torquebox.security.as" );
}
