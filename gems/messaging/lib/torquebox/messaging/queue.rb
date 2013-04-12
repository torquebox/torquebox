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

require 'torquebox/messaging/destination'
require 'torquebox/service_registry'

module TorqueBox
  module Messaging
    class Queue < Destination

      class << self

        # Creates the queue, starts and return a Queue object.
        #
        # @param name The name of the queue
        # @param options Optional parameters (a Hash), including:
        # @option options [String] :selector The selector for the queue
        # @option options [Boolean] :durable If the queue should be durable
        # @option options [Boolean] :exported If the queue should be visible in remote JNDI lookups
        # @return [Queue] if the service is created and started
        # @return [nil] if the service is not created in the specified time (30 s)
        def start(name, options={})
          selector = options.fetch(:selector, "")
          durable = options.fetch(:durable, true)
          exported = options.fetch(:exported, false)

          with_destinationizer do |destinationizer|
            latch = destinationizer.create_queue(name, durable, selector, exported)
            return nil unless TorqueBox::Messaging::Destination.wait_for_latch(latch)
          end

          new(name, options)
        end
      end

      # Publishes a message and waits for the reply
      #
      # @param message The message to publish
      # @param options Optional parameters (a Hash)
      # @return Replied message
      def publish_and_receive(message, options={})
        result = nil
        with_session do |session|
          result = session.publish_and_receive(self, message,
                                               normalize_options(options))
        end
        result
      end

      # Waits for a message and replies
      #
      # @param options Optional parameters (a Hash)
      # @param block The block to handle the received message. The return value of the block will be send back to the queue.
      # @return [void]
      def receive_and_publish(options={}, &block)
        with_session do |session|
          session.receive_and_publish(self, normalize_options(options), &block)
        end
      end

      # Returns true if queue is paused, false
      # otherwise.
      def paused?
        with_queue_control do |control|
          control.is_paused
        end
      end

      # Pauses a queue.
      #
      # Messages put into a queue will not be delivered even
      # if there are connected consumers.
      #
      # When executed on a paused queue, nothing happens.
      #
      # @return [void]
      def pause
        with_queue_control do |control|
          control.pause
        end
      end

      # Resumes a queue after it was paused.
      # When executed on a active queue, nothing happens.
      #
      # @return [void]
      def resume
        with_queue_control do |control|
          control.resume
        end
      end

      # Removes messages from the queue.
      #
      # @param filter [String] Expression in a SQL92-like syntax based on properties set on the messages. If not set all messages will be removed.
      #
      # @example Remove messages with type property set
      #
      #   @queue.remove_messages("type = 'tomatoe' OR type = 'garlic'")
      #
      # @return [Integer] Number of removed messages
      def remove_messages(filter = nil)
        with_queue_control do |control|
          control.remove_messages(filter)
        end
      end

      # Removes message from the queue by its id.
      #
      # @param id [String] ID of the message
      # @return [Boolean] true if the message was removed, false otherwise.
      def remove_message(id)
        with_queue_control do |control|
          control.remove_message(id)
        end
      end

      # Counts messages in the queue.
      #
      # @param filter [String] Expression in a SQL92-like syntax based on properties set on the messages. If not set all messages will be counted.
      #
      # @example Count messages with :type property set to 'tomatoe' or 'garlic'
      #
      #   @queue.count_messages("type = 'tomatoe' OR type = 'garlic'")
      #
      # @return [Fixnum] The number of counted messages
      def count_messages(filter = nil)
        with_queue_control do |control|
          control.count_messages(filter)
        end
      end

      # Expires messages from the queue.
      #
      # @param filter [String] Expression in a SQL92-like syntax based on properties set on the messages. If not set all messages will be expired.
      #
      # @return [Fixnum] The number of expired messages
      def expire_messages(filter = nil)
        with_queue_control do |control|
          control.expire_messages(filter)
        end
      end

      # Expires message from the queue by its id.
      #
      # @return [Boolean] Returns true if the message was expired, false otherwise.
      def expire_message(id)
        with_queue_control do |control|
          control.expire_message(id)
        end
      end

      # Sends message to dead letter address.
      #
      # @return [Boolean] Returns true if the message was sent, false otherwise.
      def send_message_to_dead_letter_address(id)
        with_queue_control do |control|
          control.send_message_to_dead_letter_address(id)
        end
      end

      # Sends messages to dead letter address.
      #
      # @param filter [String] Expression in a SQL92-like syntax based on properties set on the messages. If not set all messages will be send.
      #
      # @return [Fixnum] The number of sent messages
      def send_messages_to_dead_letter_address(filter = nil)
        with_queue_control do |control|
          control.send_messages_to_dead_letter_address(filter)
        end
      end

      # Returns the consumer count connected to the queue.
      #
      # @return [Fixnum] The number of consumers
      def consumer_count
        with_queue_control do |control|
          control.consumer_count
        end
      end

      # Returns the scheduled messages count for this queue.
      #
      # @return [Fixnum] The number of scheduled messages
      def scheduled_messages_count
        with_queue_control do |control|
          control.scheduled_count
        end
      end

      # Moves messages from the queue to another queue specified in the
      # +queue_name+ parameter. Optional +reject_duplicates+ parameter
      # specifies if the duplicates should be rejected.
      #
      # @param queue_name [String] The name of the queue to move the messages to
      # @param filter [String] Parameter to limit messages to move. If provided nil or empty string, *all messages* will be moved.
      # @param reject_duplicates [Boolean] Specifies if the duplicates should be rejected
      # @return [Fixnum] The number of moved messages
      def move_messages(queue_name, filter = nil, reject_duplicates = false)
        with_queue_control do |control|
          control.move_messages(filter, queue_name, reject_duplicates)
        end
      end

      # Moves message for specific id from the queue to another queue
      # specified in the queue_name parameter.
      #
      # @param queue_name [String] The name of the queue to move the messages to
      # @param id [String] Message ID
      # @param reject_duplicates [Boolean] Specifies if the duplicates should be rejected
      # @return [Boolean] true if the message was moved,false otherwise
      def move_message(queue_name, id, reject_duplicates = false)
        with_queue_control do |control|
          control.move_message(id, queue_name, reject_duplicates)
        end
      end

      # Returns current expiry address.
      #
      # @return [String] Current expiry address. Please note that the destination contains 'jms.queue' or 'jms.topic' prefixes.
      def expiry_address
        with_queue_control do |control|
          control.expiry_address
        end
      end

      # Sets the expiry address.
      #
      # Please note that you need to provide the
      # *full address* containing the destination name and
      # jms.queue or jms.topic prefixes, for example:
      #
      # @example Set the destination to /queues/customexpire
      #
      #   @queue.expiry_address = "jms.queue./queues./customexpire"
      #
      # @example Set the destination to /topics/customexpire
      #
      #   @queue.expiry_address = "jms.topic./topics./customexpire"
      #
      # @return [String] Current expiry address
      def expiry_address=(address)
        with_queue_control do |control|
          control.set_expiry_address(address)
        end

        expiry_address
      end

      # Returns current dead letter address.
      #
      # @return [String] Current dead letter address. Please note that the destination contains 'jms.queue' or 'jms.topic' prefixes,
      def dead_letter_address
        with_queue_control do |control|
          control.dead_letter_address
        end
      end

      # Sets the dead letter address.
      #
      # Please note that you need to provide the
      # *full address* containing the destination name and
      # +jms.queue+ or +jms.topic+ prefixes, for example:
      #
      # @example Set the destination to /queues/customdead
      #
      #   @queue.dead_letter_address = "jms.queue./queues./customdead"
      #
      # @example Set the destination to /topics/customdead
      #
      #   @queue.dead_letter_address = "jms.topic./topics./customdead"
      #
      # @return [String] Current dead letter address
      def dead_letter_address=(address)
        with_queue_control do |control|
          control.set_dead_letter_address(address)
        end

        dead_letter_address
      end


      def to_s
        "[Queue: #{super}]"
      end

      # @api private
      #
      # Retrieves the JMSQueueControl implementation for current
      # queue.
      def with_queue_control
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default") do |server|
          yield server.management_service.get_resource("jms.queue.#{_dump(nil)}")
        end
      end
    end
  end
end
