# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

module TorqueBox
  module Messaging
    # A Future encapsulates the result of a long running
    # process, and is used in conjunction with a {FutureResponder}. 
    class Future
      # We can't really do no timeout - 1ms is as close as we can get.
      NO_TIMEOUT = 1
      
      # Returns the remote error (if any)
      attr_reader :error
      attr_reader :correlation_id
      attr_accessor :default_result_timeout
      
      # Returns all of the statuses seen by this future as an array.
      attr_reader :all_statuses
      
      # @param [TorqueBox::Messaging::Queue] response_queue The queue
      #   where response messages are to be received.
      # @param [Hash] options Additional options
      # @option options [String] :correlation_id (Future.unique_id) The correlation_id used on
      #   the messages to uniquely identify the call they are for.
      # @option options [Integer] :default_result_timeout (30_000) The timeout
      #   used by default for the receive call. The processing must at
      #   least start before the timeout expires, and finish before 2x
      #   this timeout.
      def initialize(response_queue, options = { })
        @queue = response_queue
        @correlation_id = options[:correlation_id] || self.class.unique_id
        @default_result_timeout = options[:default_result_timeout] || 30_000
        @all_statuses = []
      end

      def started?
        receive unless @started
        !!@started
      end

      def complete?
        receive unless @complete || @error
        !!@complete
      end

      def error?
        receive unless @complete || @error
        !!@error
      end

      # Returns the latest response from the remote processor, if
      # any. Status reporting is optional, and must be handled by the
      # processed task itself.
      # @see FutureResponder#status
      def status
        @prior_status = retrieve_status
      end
      
      # Returns true if the status has changed since the last call to
      # {#status}. 
      def status_changed?
        @prior_status != retrieve_status
      end
      
      # Attempts to return the remote result.
      # @param [Integer] timeout The processing must at least start
      #   before the timeout expires, and finish before 2x this timeout.
      # @raise [TimeoutException] if the timeout expires when
      #   receiving the result
      # @return the remote result
      def result(timeout = default_result_timeout)
        receive( timeout ) unless @started
        raise TimeoutException.new( "timeout expired waiting for processing to start" ) unless @started
        receive( timeout ) unless @complete || @error
        raise TimeoutException.new( "timeout expired waiting for processing to finish" ) unless @complete || @error
        raise @error if @error
        @result
      end

      # Delegates to {#result} with the default timeout.
      def method_missing(method, *args, &block)
        result.send( method, *args, &block )
      end

      # @return [String] a unique id useful for correlating a
      #   result to its call
      def self.unique_id
        java.util.UUID.randomUUID.to_s
      end

      protected
      def retrieve_status
        receive unless @complete || @error
        @status
      end
      
      def receive(timeout = NO_TIMEOUT)
        response = @queue.receive( :timeout => timeout, :selector => "JMSCorrelationID = '#{@correlation_id}'" )

        if response
          @started = true
          if response.has_key?( :status )
            @status = response[:status]
            @all_statuses << @status
          end
          @complete = response.has_key?( :result )
          @result ||= response[:result]
          @error ||= response[:error]
        end
      end

    end

    class TimeoutException < RuntimeError; end

  end
end
