package org.torquebox.messaging.deployers;

import java.util.*;
import java.io.InputStream;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.deployers.DeployerRuby;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.yaml.snakeyaml.Yaml;

public class MessagingYamlParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

    public MessagingYamlParsingDeployer() {
        super(MessageProcessorMetaData.class);
        setName("messaging.yml");
        setStage(DeploymentStages.POST_CLASSLOADER);
        addRequiredInput( DeployerRuby.class );
    }

    @Override
    protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {
        for (MessageProcessorMetaData metadata: parser.parse(file.openStream())) {
            AttachmentUtils.multipleAttach(unit, metadata, metadata.getName());
        }
        return null;
    }


    private Parser parser = new Parser();

    static class Parser {
        
        List<MessageProcessorMetaData> parse (InputStream in) throws Exception {
            try {
                Yaml yaml = new Yaml();
                Object data = yaml.load(in);
                if (data instanceof String) {
                    String s = (String) data;
                    if (s.trim().length() == 0) {
                        return Collections.EMPTY_LIST;
                    } else {
                        throw new RuntimeException("Invalid configuration");
                    }
                }
                return parse ((Map<String,Object>) data);
            } finally {
                in.close();
            }
        }

        List<MessageProcessorMetaData> parse (Map<String,Object> data)  {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (String destination: data.keySet()) {
                Object value = data.get(destination);
                if (value instanceof Map) {
                    Map<String,Map> handlers = (Map<String,Map>) value;
                    for (String handler: handlers.keySet()) {
                        result.add( subscribe( handler, destination, handlers.get(handler) ) );
                    }
                } else if (value instanceof List) {
                    List handlers = (List) value;
                    for (Object v: handlers) {
                        if (v instanceof String) {
                            String handler = (String) v;
                            result.add( subscribe( handler, destination, Collections.EMPTY_MAP ) );
                        } else { // it's a Map with one entry
                            Map.Entry<String,Map> handler = ((Map<String,Map>) v).entrySet().iterator().next();
                            result.add( subscribe( handler.getKey(), destination, handler.getValue() ) );
                        }
                    }
                } else { // it's a String
                    String handler = (String) value;
                    result.add( subscribe( handler, destination, Collections.EMPTY_MAP ) );
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
