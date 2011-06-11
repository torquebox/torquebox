module TorqueBox
  module Messaging
    class FutureResponder

      def initialize(response_queue, correlation_id, message_ttl = 600_000)
        @queue = response_queue
        @correlation_id = correlation_id
        @message_ttl = message_ttl
      end

      def started
        publish( :started => true )
      end

      def complete(result)
        publish( :result => result, :priority => :high )
      end

      def error(error)
        publish( :error => error, :priority => :high )
      end

      def respond
        started
        complete( yield )
      rescue Exception => e
        error( e )
      end
      
      protected
      def publish(message)
        @queue.publish( message, :correlation_id => @correlation_id, :ttl => @message_ttl )
      end
    end
  end
end
