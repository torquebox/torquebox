# A message processor that echos any messages sent to it back to
# another queue specified by the response_queue configuration option
module Torquebox
  module Messaging
    class EchoProcessor < TorqueBox::Messaging::MessageProcessor

      def initialize(options={})
        @response_queue = TorqueBox::Messaging::Queue.new(options['response_queue'])
      end

      def on_message(body)
        @response_queue.publish(body)
      end

    end
  end
end
