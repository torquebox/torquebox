require 'torquebox/messaging/core/live_destination'
require 'torquebox/messaging/core/connection_factory'

module TorqueBox
  module Messaging
    module Core
      class LiveQueue < LiveDestination
        def initialize(jms_connection_factory, jms_queue)
          super( ConnectionFactory.new( jms_connection_factory ), Queue.new( jms_queue ) )
        end

        def publish_and_receive(message, options={})
          result = nil
          connection_factory.with_new_session do |session|
            result = session.publish_and_receive(name, message,
                                                 normalize_options(options))
          end
          result
        end
  
        def receive_and_publish(options={}, &block)
          connection_factory.with_new_session do |session|
            session.receive_and_publish(name, normalize_options(options), &block)
            session.commit if session.transacted?
          end
        end

        def to_s
          "[LiveQueue: #{super}]"
        end
      end
    end
  end
end
