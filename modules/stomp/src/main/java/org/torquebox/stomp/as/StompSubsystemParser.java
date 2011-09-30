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

package org.torquebox.stomp.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;

import java.util.Collections;
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

public class StompSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private static final StompSubsystemParser INSTANCE = new StompSubsystemParser();

    public static StompSubsystemParser getInstance() {
        return INSTANCE;
    }

    private StompSubsystemParser() {
    }

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        // requireNoAttributes( reader );

        final ModelNode address = new ModelNode();
        address.add( SUBSYSTEM, StompExtension.SUBSYSTEM_NAME );
        address.protect();

        ModelNode subsystem = StompSubsystemAdd.createOperation( address );

        String bindingRef = null;

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute( reader, i );
            final String value = reader.getAttributeValue( i );
            final String name = reader.getAttributeLocalName( i );
            if (name.equals( "socket-binding" )) {
                bindingRef = value;
            } else {
                throw unexpectedAttribute( reader, i );
            }
        }

        if (bindingRef == null) {
            throw missingRequired( reader, Collections.singleton( "socket-binding" ) );
        }

        subsystem.get( "socket-binding" ).set( bindingRef );

        requireNoContent( reader );

        list.add( subsystem );
    }

    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement( Namespace.CURRENT.getUriString(), false );
        writer.writeAttribute( "socket-binding", context.getModelNode().get( "socket-binding" ).asString() );
        writer.writeEndElement();
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.stomp.as" );

}
