/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.security.auth.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.torquebox.security.as.Namespace;
import org.torquebox.security.as.SecurityExtension;

public class AuthSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private static final AuthSubsystemParser INSTANCE = new AuthSubsystemParser();

    public static AuthSubsystemParser getInstance() {
        return INSTANCE;
    }

    private AuthSubsystemParser() {
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter writer,
            SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement( Namespace.CURRENT.getUriString(), false );
        writer.writeEndElement();
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list)
            throws XMLStreamException {
        requireNoAttributes( reader );
        requireNoContent( reader );

        final ModelNode address = new ModelNode();
        address.add( SUBSYSTEM, SecurityExtension.SUBSYSTEM_NAME );
        address.protect();

        list.add( AuthSubsystemAdd.createOperation( address ) );
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );
}
