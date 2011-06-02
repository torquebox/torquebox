require 'torquebox/messaging/destination'
require 'torquebox/messaging/connection_factory'
require 'torquebox/service_registry'

module TorqueBox
  module Messaging
    class Topic < Destination

      def self.start( name, options={} )
        jndi = options.fetch( :jndi, [].to_java(:string) )
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.jms.manager") do |server|
          server.createTopic( false, name, jndi )
        end
        new( name )
      end

      def stop
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.jms.manager") do |server|
          server.destroyTopic( name )
        end
      end

      def to_s
        "[Topic: #{super}]"
      end
    end
  end
end
