puts "loading metadata_builder.rb from #{__FILE__}"

module TorqueBox
  module Messaging
    module MetaData
      class Builder
        attr_reader :processors
        def initialize(&block)
          @processors = []
          block.call( self ) if block
        end
        
        def evaluate_file(file)
          evaluate( File.read( file ), file ) 
        end
        
        def evaluate(config,file="(eval)")
          instance_eval( config, file )
        end

        def self.evaluate_file(file)
          builder = Builder.new()
          builder.evaluate_file( file )
          builder.processors
        end

        def subscribe(processor, destination_name, opts={})
          metadata = Java::org.torquebox.messaging.metadata::MessageProcessorMetaData.new()
          case ( processor )
            when Class
              metadata.ruby_class_name = processor.name
            when String, Symbol
              processor_str = processor.to_s
              metadata.ruby_class_name   = Java::org.torquebox.common.util::StringUtils.camelize( processor_str )
              metadata.ruby_require_path = Java::org.torquebox.common.util::StringUtils.underscore( processor_str )
            else
              throw "Unable to configure message processor #{processor}"
          end 
          metadata.destination_name = destination_name
          metadata.message_selector = opts[:filter]
          config = opts[:config] || {}
          processor_config = Marshal.dump( config )
          config_bytes = processor_config.to_java_bytes
          metadata.ruby_config = config_bytes
          @processors << metadata
        end
      end
    end
  end
end

