class Java::org.torquebox.enterprise.ruby.messaging::Client
end

module TorqueBox
  module Messaging

    class Client 
      def self.connect(opts={},&block)
        factory = Java::org.torquebox.enterprise.ruby.messaging::ClientFactory.new
        factory.naming_provider_url          = opts[:naming_provider_url]          unless ( opts[:naming_provider_url].nil? )
        factory.context_factory_class_name   = opts[:context_factory_class_name]   unless ( opts[:context_factory_class_name].nil? )
        factory.url_package_prefixes         = opts[:url_package_prefixes]         unless ( opts[:url_package_prefixes].nil? )
        factory.connection_factory_jndi_name = opts[:connection_factory_jndi_name] unless ( opts[:connection_factory_jndi_name].nil? )
        client = factory.create_client
        client.connect
        if ( block )
          begin
            block.call( client )
            client.commit
          rescue => e
            client.rollback
            raise e
          ensure
            client.close
          end
        end
      end

    end

  end
end
