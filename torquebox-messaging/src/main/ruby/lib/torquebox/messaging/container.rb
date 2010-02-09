
module Java
  class org::torquebox::enterprise::ruby::messaging::container::Container
    def wait_until(signal)
      puts "waiting for #{signal}"
      keep_running = true
      Signal.trap( signal ) {
        keep_running = false
      }
      while ( keep_running )
        sleep( 1 )
      end 
      stop
      destroy
    end
    
  end
end

module TorqueBox
  module Messaging
    class Container < Java::org.torquebox.enterprise.ruby.messaging.container::Container
      class Config

        attr_accessor :naming_provider_url
        attr_accessor :context_factory_class_name
        attr_accessor :url_package_prefixes

        attr_accessor :connection_factory_jndi_name
        attr_accessor :agent_configs

        def initialize(&block)
          @agent_configs = []
          instance_eval &block if block
        end

        def agents(&block)
          AgentConfig.new(self, &block)
        end

        class AgentConfig
          def initialize(config, &block)
            @config = config
            instance_eval &block if block
          end
         
          def map(destination_name, ruby_class_name)
            agent_config = Java::org.torquebox.enterprise.ruby.messaging.metadata::MessageDrivenAgentConfig.new
            agent_config.destination_name = destination_name
            agent_config.ruby_class_name = ruby_class_name
            @config.agent_configs << agent_config
          end
        end # AgentConfig
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

        config.agent_configs.each do |agent_config|
          container.add_message_driven_agent( agent_config )
        end
        container.create
        container
      end

    end # Container

  end
end
