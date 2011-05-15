require 'torquebox/messaging/core/live_destination'
require 'torquebox/messaging/core/connection_factory'

module TorqueBox
  module Messaging
    module Core
      class LiveQueue < LiveDestination
        def initialize(jms_connection_factory, jms_queue)
          super( ConnectionFactory.new( jms_connection_factory ), Queue.new( jms_queue ) )
        end

        def to_s
          "[LiveQueue: #{super}]"
        end
      end
    end
  end
end
