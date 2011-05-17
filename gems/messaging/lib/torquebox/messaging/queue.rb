require 'torquebox/messaging/destination'
require 'torquebox/messaging/connection_factory'

module TorqueBox
  module Messaging
    class Queue < Destination

      def publish_and_receive(message, options={})
        result = nil
        connection_factory.with_new_connection do |connection|
          connection.with_new_session do |session|
            result = session.publish_and_receive(self, message,
                                                 normalize_options(options))
          end
        end
        result
      end

      def receive_and_publish(options={}, &block)
        connection_factory.with_new_connection do |connection|
          connection.with_new_session do |session|
            session.receive_and_publish(self, normalize_options(options), &block)
            session.commit if session.transacted?
          end
        end
      end

      def to_s
        "[Queue: #{super}]"
      end
    end
  end
end
