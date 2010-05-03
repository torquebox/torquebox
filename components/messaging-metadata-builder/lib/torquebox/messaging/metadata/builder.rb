
module TorqueBox
  module Messaging
    module MetaData
      class Builder
        attr_reader :processors
        def initialize(&block)
          @processors = []
          instance_eval &block if block
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
          @processors << metadata
        end
      end
    end
  end
end

