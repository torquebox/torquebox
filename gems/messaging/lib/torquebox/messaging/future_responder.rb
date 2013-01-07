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
    # A FutureResponder encapsulates sending the results of a long
    # running process to a {Future}.
    class FutureResponder

      # @param [TorqueBox::Messaging::Queue] response_queue The queue
      #   where response messages are to be published.
      # @param [String] correlation_id The correlation_id used on
      #   the messages to uniquely identify the call they are for.
      # @param [Integer] message_ttl The time-to-live used on messages
      #   to prevent them from staying in the queue indefinately if
      #   the result is never accessed.
      def initialize(response_queue, correlation_id, message_ttl = 600_000)
        @queue = response_queue
        @correlation_id = correlation_id
        @message_ttl = message_ttl
      end

      # Signal that processing has started.
      def started
        send_response(:low)
      end

      # Report the current status back to the client. The status value
      # is application specific.
      def status=(status)
        @status_set = true
        @status = status
        send_response
      end
      
      # Signal that processing has completed.
      # @param The result of the operation.
      def result=(result)
        @result_set = true
        @result = result
        send_response :high 
      end

      # Signal that an error occurred during processing.
      # @param [Exception] The error!
      def error=(error)
        @error = error
        send_response :high 
      end
      
      # Handles started/complete/error for you around the given
      # block. The current responder is avaiable inside the block via
      # {.current}, which is useful for sending {#status} messages.
      def respond
        started
        Thread.current[:future_responder] = self
        self.result = yield 
      rescue Exception => e
        self.error = e
        $stderr.puts "FutureResponder#respond: An error occured: ", e
        $stderr.puts e.backtrace.join( "\n" )
      end

      # Convenience method that returns the thread local
      # responder. Only valid inside a block passed to {#respond}.
      def self.current
        Thread.current[:future_responder]
      end

      # Convenience method that allows you to send a status message 
      # via the {.current} responder. Only valid inside a block passed
      # to {#respond}.
      def self.status=(status)
        current.status = status if current
      end
      
      protected
      def send_response(priority = :normal)
        message = {}
        message[:status] = @status if @status_set
        message[:error] = @error if @error
        message[:result] = @result if @result_set
        @queue.publish( message,
                        :correlation_id => @correlation_id,
                        :ttl => @message_ttl,
                        :priority => priority,
                        :encoding => :marshal,
                        :tx => false # can't be a part of the task's tx!
                        )
      rescue TypeError => ex
        $stderr.puts "FutureResponder#send_response: Warning: unable to marshal #{@result.inspect}"
      end
    end
  end
end
