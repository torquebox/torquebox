 module TorqueBox
  module Messaging
    class FutureResult

      attr_reader :error
      attr_reader :correlation_id
      
      def initialize(response_queue, correlation_id = self.class.unique_id)
        @queue = response_queue
        @correlation_id = correlation_id
      end

      def started?
        receive unless @started
        @started
      end
      
      def complete?
        receive unless @complete || @error
        @complete
      end
      
      def error?
        receive unless @complete || @error
        !!@error
      end

      def result(timeout = 0)
        receive( timeout ) unless @started
        raise TimeoutException.new( "timeout expired waiting for processing to start" ) unless @started
        receive( timeout ) unless @complete || @error
        raise TimeoutException.new( "timeout expired waiting for processing to finish" ) unless @complete || @error
        raise @error if @error
        @result
      end

      def self.unique_id
        java.util.UUID.randomUUID.to_s
      end
      
      protected
      def receive(timeout = 1)
        response = @queue.receive( :timeout => timeout, :selector => "JMSCorrelationID = '#{@correlation_id}'" )
        
        if response
          @started = true
          @complete = response.has_key?( :result )
          @result = response[:result]
          @error = response[:error]
        end
      end

    end

    class TimeoutException < RuntimeError; end

  end
end
