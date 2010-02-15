module TorqueBox
  module Messaging
    class Container 
      class Config

        attr_accessor :connection_factory_jndi_name
        attr_accessor :consumer_configs

        def self.create(&block)
          config = Config.new( &block )
          config.consumer_configs
        end

        def initialize(&block)
          @consumer_configs = []
          @naming_provider_url = nil
          @context_factory_class_name = nil
          @url_package_prefixes = nil
          instance_eval &block if block
        end


        def naming_provider_url(val=nil)
          unless val.nil?
            @naming_provider_url = val
          end
          @naming_provider_url
        end

        def context_factory_class_name(val=nil)
          unless val.nil?
            @context_factory_class_name = value
          end
          @context_factory_class_name
        end

        def url_package_prefixes(val=nil)
          unless val.nil?
            @url_package_prefixes = val
          end
          @url_package_prefixes
        end

        def connection_factory_jndi_name(val=nil)
          unless val.nil?
            @connection_factory_jndi_name = val
          end
          @connection_factory_jndi_name
        end

        def consumers(&block)
          ConsumerConfig.new(self, &block)
        end

        class ConsumerConfig
          def initialize(config, &block)
            @config = config
            instance_eval &block if block
          end
         
          def map(destination_name, ruby_class_name)
            consumer_config = Java::org.torquebox.messaging::MessageDrivenConsumerConfig.new
            consumer_config.destination_name = destination_name
            consumer_config.ruby_class_name = ruby_class_name.to_s
            @config.consumer_configs << consumer_config
          end
        end # ConsumerConfig
      end # Config

    end # Container

  end
end
