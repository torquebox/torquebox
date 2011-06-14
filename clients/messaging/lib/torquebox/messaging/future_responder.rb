# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
    # running process to a {FutureResult}.
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
        publish( :started => true )
      end

      # Signal that processing has completed.
      # @param The result of the operation.
      def complete(result)
        publish( :result => result, :priority => :high )
      end

      # Signal that an error occurred during processing.
      # @param [Exception] The error!
      def error(error)
        publish( :error => error, :priority => :high )
      end

      # Handles started/complete/error for you around the given block.
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
