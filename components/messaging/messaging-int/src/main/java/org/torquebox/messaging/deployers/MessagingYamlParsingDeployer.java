package org.torquebox.messaging.deployers;

import java.util.*;
import java.io.InputStream;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.common.util.StringUtils;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.yaml.snakeyaml.Yaml;


/**
 * <pre>
 * Stage: PARSE
 *    In: messaging.yml
 *   Out: MessageProcessorMetaData
 * </pre>
 *
 * Creates MessageProcessorMetaData instances from messaging.yml
 */
public class MessagingYamlParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

    public MessagingYamlParsingDeployer() {
        super(MessageProcessorMetaData.class);
        setName("messaging.yml");
    }

    @Override
    protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {
        for (MessageProcessorMetaData metadata: Parser.parse(file.openStream())) {
            AttachmentUtils.multipleAttach(unit, metadata, metadata.getName());
        }
        return null;
    }


    public static class Parser {

        public static List<MessageProcessorMetaData> parse (InputStream in) throws Exception {
            Parser parser = new Parser();
            Yaml yaml = new Yaml();
            try {
                return parser.parseYaml(yaml.load(in));
            } finally {
                in.close();
            }
        }
        
        public static List<MessageProcessorMetaData> parse (String input) throws Exception {
            Parser parser = new Parser();
            Yaml yaml = new Yaml();
            return parser.parseYaml(yaml.load(input));
        }

        List<MessageProcessorMetaData> parseYaml (Object data) throws Exception {
            if (data instanceof String) {
                String s = (String) data;
                if (s.trim().length() == 0) {
                    return Collections.EMPTY_LIST;
                } else {
                    throw new RuntimeException("Invalid configuration");
                }
            }
            return parseDestinations ((Map<String,Object>) data);
        }

        List<MessageProcessorMetaData> parseDestinations (Map<String,Object> data)  {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (String destination: data.keySet()) {
                Object value = data.get(destination);
                if (value instanceof Map) {
                    result.addAll( parseHandlers( destination, (Map<String,Map>) value ) );
                } else if (value instanceof List) {
                    result.addAll( parseHandlers( destination, (List) value ) );
                } else { 
                    result.add( parseHandler( destination, (String) value ) );
                }
            }
            return result;
        }

        MessageProcessorMetaData parseHandler (String destination, String handler) {
            return subscribe( handler, destination, Collections.EMPTY_MAP );
        }

        List<MessageProcessorMetaData> parseHandlers (String destination, Map<String,Map> handlers) {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (String handler: handlers.keySet()) {
                result.add( subscribe( handler, destination, handlers.get(handler) ) );
            }
            return result;
        }

        List<MessageProcessorMetaData> parseHandlers (String destination, List handlers) {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (Object v: handlers) {
                if (v instanceof String) {
                    result.add( parseHandler( destination, (String) v ) );
                } else {
                    result.addAll( parseHandlers( destination, (Map<String,Map>) v ) );
                }
            }
            return result;
        }

        MessageProcessorMetaData subscribe (String handler, String destination, Map options) {
            if (options==null) options = Collections.EMPTY_MAP;
            MessageProcessorMetaData result = new MessageProcessorMetaData();
            result.setRubyClassName( StringUtils.camelize( handler ) );
            result.setRubyRequirePath( StringUtils.underscore( handler ) );
            result.setDestinationName( destination );
            result.setMessageSelector( (String) options.get( "filter" ) );
            result.setRubyConfig( (Map) options.get( "config" ) );
            return result;
        }

    }
}
