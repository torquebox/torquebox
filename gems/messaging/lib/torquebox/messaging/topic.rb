require 'torquebox/messaging/destination'
require 'torquebox/messaging/connection_factory'
require 'torquebox/service_registry'

module TorqueBox
  module Messaging
    class Topic < Destination

      def self.start( name, options={} )
        jndi = options.fetch( :jndi, [] )
        TorqueBox::ServiceRegistry.lookup("JMSServerManager") do |server|
          server.createTopic( false, name, jndi )
        end
        new( name )
      end

      def stop
        TorqueBox::ServiceRegistry.lookup("JMSServerManager") do |server|
          server.destroyTopic( name )
        end
      end

      def to_s
        "[Topic: #{super}]"
      end
    end
  end
end
