package org.torquebox.auth.as;

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

public class AuthSubsystemParser  implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private static final AuthSubsystemParser INSTANCE = new AuthSubsystemParser();

    public static AuthSubsystemParser getInstance() {
        return INSTANCE;
    }
    
    private AuthSubsystemParser() {
	}


	@Override
	public void writeContent(XMLExtendedStreamWriter writer,
			SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
        writer.writeEndElement();
}

	@Override
	public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list)
			throws XMLStreamException {
        log.info( "torquebox-auth begin readElement" );
        requireNoAttributes(reader);
        requireNoContent(reader);
        
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, AuthExtension.SUBSYSTEM_NAME);
        address.protect();

        list.add(AuthSubsystemAdd.createOperation(address));
        log.info( "torquebox-auth end readElement" );
	}

    private static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );
}
