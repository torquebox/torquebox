
class Java::org.torquebox.messaging.container::Container
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
    class Container < Java::org.torquebox.messaging.container::Container
      def self.new(&block)
        config = Config.new( &block )
        factory = Java::org.torquebox.messaging.container::ContainerFactory.new

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

require 'torquebox/messaging/config'
