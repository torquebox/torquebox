require 'torquebox/messaging/live_destination'
require 'torquebox/messaging/connection_factory'

module TorqueBox
  module Messaging
    class LiveTopic < LiveDestination
      def initialize(jms_connection_factory, jms_topic)
        super( ConnectionFactory.new( jms_connection_factory ), Topic.new( jms_topic ) )
      end

      def to_s
        "[LiveTopic: #{super}]"
      end
    end
  end
end
