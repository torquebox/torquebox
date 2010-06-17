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
          puts "evaluating!"
          puts config
          puts "evaluating!"
          instance_eval( config, file )
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
          processor_config = Marshal.dump( opts[:config] || {} )
          metadata.ruby_config = processor_config.to_s.to_java
          @processors << metadata
        end
      end
    end
  end
end

