require 'torquebox/messaging/client'
require 'base64'

module TorqueBox
  module Messaging

    class Task

      def self.queue_name
        '/queues/torquebox/tasks/' + name[0...-4].downcase
      end

      def self.async(method, payload={})
        TorqueBox::Messaging::Client.connect() do |session|
          queue    = session.create_queue( queue_name )
          producer = session.create_producer( queue )

          message = session.create_text_message
          message.set_string_property( 'method', method.to_s )
          marshalled = Marshal.dump( payload )
          encoded = Base64.encode64( marshalled )
          message.text = encoded

          producer.send( message )
          session.commit
        end
      end

      def process!(message)
        encoded = message.text
        serialized = Base64.decode64( encoded )
        payload = Marshal.restore( serialized )
        method = message.get_string_property( 'method' )
        self.send method.to_sym, payload
      end

    end

  end
end

