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

      def self.start( name, options={} )
        selector = options.fetch( :selector, "" )
        durable  = options.fetch( :durable,  true )
        jndi     = options.fetch( :jndi,     [].to_java(:string) )
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager") do |server|
          server.createQueue( false, name, selector, durable, jndi )
        end
        new( name, options )
      end

      def stop
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager") do |server|
          server.destroyQueue( name )
        end
      end

      def publish_and_receive(message, options={})
        result = nil
        with_session do |session|
          result = session.publish_and_receive(self, message,
                                               normalize_options(options))
        end
        result
      end

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
      def pause
        with_queue_control do |control|
          control.pause
        end
      end

      # Resumes a queue after it was paused.
      # When executed on a active queue, nothing happens.
      def resume
        with_queue_control do |control|
          control.resume
        end
      end

      # Removes messages from the queue.
      #
      # Accepts optional :filter parameter to remove only
      # selected messages from the queue. By default
      # *all* messages will be removed.
      #
      # The :filter parameter is a String where you define
      # expressions in a SQL92-like syntax based on properties
      # set on the messages.
      #
      # Example:
      #
      # type = 'tomatoe' OR type = 'garlic'
      #
      # This will remove messages with :type property set
      # to 'tomatoe' or 'garlic'
      #
      # This function returns number of removed messages.
      def remove_messages(filter = nil)
        with_queue_control do |control|
          control.remove_messages(filter)
        end
      end

      # Removes message from the queue by its id.
      #
      # Returns +true+ if the message was removed,
      # +false+ otherwise.
      def remove_message(id)
        with_queue_control do |control|
          control.remove_message(id)
        end
      end

      # Counts messages in the queue.
      #
      # Accepts optional :filter parameter to count only
      # selected messages in the queue. By default
      # all messages will be counted.
      #
      # The :filter parameter is a String where you define
      # expressions in a SQL92-like syntax based on properties
      # set on the messages.
      #
      # Example:
      #
      # type = 'tomatoe' OR type = 'garlic'
      #
      # This will count messages with :type property set
      # to 'tomatoe' or 'garlic'
      def count_messages(filter = nil)
        with_queue_control do |control|
          control.count_messages(filter)
        end
      end

      # Expires messages from the queue.
      #
      # Accepts optional :filter parameter to expire only
      # selected messages in the queue. By default
      # all messages will be expired.
      #
      # Returns number of expired messaged.
      def expire_messages(filter = nil)
        with_queue_control do |control|
          control.expire_messages(filter)
        end
      end

      # Expires message from the queue by its id.
      #
      # Returns +true+ if the message was expired,
      # +false+ otherwise.
      def expire_message(id)
        with_queue_control do |control|
          control.expire_message(id)
        end
      end

      # Sends message to dead letter address.
      #
      # Returns +true+ if the message was sent,
      # +false+ otherwise.
      def send_message_to_dead_letter_address(id)
        with_queue_control do |control|
          control.send_message_to_dead_letter_address(id)
        end
      end

      # Sends messages to dead letter address.
      #
      # Accepts optional :filter parameter to send only
      # selected messages from the queue. By default
      # all messages will be send.
      #
      # Returns number of sent messaged.
      def send_messages_to_dead_letter_address(filter = nil)
        with_queue_control do |control|
          control.send_messages_to_dead_letter_address(filter)
        end
      end

      # Returns the consumer count connected to the queue.
      def consumer_count
        with_queue_control do |control|
          control.consumer_count
        end
      end

      # Returns the scheduled messages count for this queue.
      def scheduled_messages_count
        with_queue_control do |control|
          control.scheduled_count
        end
      end

      # Moves messages from the queue to another queue specified in the
      # +queue_name+ parameter. Optional +reject_duplicates+ parameter
      # specifies if the duplicates should be rejected.
      #
      # The +filter+ parameter makes it possible to limit messages to
      # move. If provided +nil+ or empty string, *all messages* will be moved.
      #
      # Returns number of moved messages.
      def move_messages(queue_name, filter = nil, reject_duplicates = false)
        with_queue_control do |control|
          control.move_messages(filter, queue_name, reject_duplicates)
        end
      end

      # Moves message for specific +id+ from the queue to another queue
      # specified in the +queue_name+ parameter. Optional +reject_duplicates+
      # parameter specifies if the duplicates should be rejected.
      #
      # Returns +true+ if the message was moved,
      # +false+ otherwise.
      def move_message(queue_name, id, reject_duplicates = false)
        with_queue_control do |control|
          control.move_message(id, queue_name, reject_duplicates)
        end
      end

      # Returns current expiry address.
      #
      # The destination contains +jms.queue+ or +jms.topic+ prefixes,
      def expiry_address
        with_queue_control do |control|
          control.expiry_address
        end
      end

      # Sets the expiry address.
      #
      # Please note that you need to provide the
      # *full address* containing the destination name and
      # +jms.queue+ or +jms.topic+ prefixes, for example:
      #
      # If you want to set the destination to /queues/customexpire, use
      # +queue.expiry_address = "jms.queue./queues./customexpire"+
      #
      # If you want to set the destination to /topics/customexpire, use
      # +queue.expiry_address = "jms.topic./topics./customexpire"+
      #
      # Returns current expiry address.
      def expiry_address=(address)
        with_queue_control do |control|
          control.set_expiry_address(address)
        end

        expiry_address
      end

      # Returns current dead letter address.
      #
      # The destination contains +jms.queue+ or +jms.topic+ prefixes,
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
      # If you want to set the destination to /queues/customdead, use
      # +queue.dead_letter_address = "jms.queue./queues./customdead"+
      #
      # If you want to set the destination to /topics/customdead, use
      # +queue.dead_letter_address = "jms.topic./topics./customdead"+
      #
      # Returns current dead letter address.
      def dead_letter_address=(address)
        with_queue_control do |control|
          control.set_dead_letter_address(address)
        end

        dead_letter_address
      end

      # Retrieves the JMSQueueControl implementation for current
      # queue.
      def with_queue_control
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default") do |server|
          yield server.management_service.get_resource("jms.queue.#{_dump(nil)}")
        end
      end

      def to_s
        "[Queue: #{super}]"
      end
    end
  end
end
