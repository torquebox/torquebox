class Java::org.torquebox.enterprise.ruby.messaging.container::Container
  def wait_until(signal)
    keep_running = true
    Signal.trap( signal ) {
      keep_running = false
    }
    while ( keep_running )
      sleep( 1 )
    end 
    stop
  end
end


module TorqueBox
  module Messaging
    class Container < Java::org.torquebox.enterprise.ruby.messaging.container::Container
      class Config

        attr_accessor :connection_factory_jndi_name
        attr_accessor :consumer_configs

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
            consumer_config = Java::org.torquebox.enterprise.ruby.messaging.metadata::MessageDrivenConsumerConfig.new
            consumer_config.destination_name = destination_name
            consumer_config.ruby_class_name = ruby_class_name.to_s
            @config.consumer_configs << consumer_config
          end
        end # ConsumerConfig
      end # Config

      def self.new(&block)
        config = Config.new( &block )
        factory = Java::org.torquebox.enterprise.ruby.messaging.container::ContainerFactory.new

        factory.context_factory_class_name = config.context_factory_class_name unless config.context_factory_class_name.nil?
        factory.url_package_prefixes       = config.url_package_prefixes       unless config.url_package_prefixes.nil?
        factory.naming_provider_url        = config.naming_provider_url        unless config.naming_provider_url.nil?

        factory.connection_factory_jndi_name = config.connection_factory_jndi_name unless config.connection_factory_jndi_name.nil?

        container  = factory.createContainer

        ruby_runtime_factory = Java::org.torquebox.common.runtime::SingletonRubyRuntimeFactory.new( JRuby.runtime )
        ruby_runtime_pool    = Java::org.torquebox.common.runtime::SharedRubyRuntimePool.new( ruby_runtime_factory )
        ruby_runtime_pool.start()
        container.ruby_runtime_pool = ruby_runtime_pool

        config.consumer_configs.each do |consumer_config|
          container.add_message_driven_consumer( consumer_config )
        end

        container.create
        container
      end

    end # Container

  end
end
