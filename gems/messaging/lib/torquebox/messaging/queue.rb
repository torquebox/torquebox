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
        new( name )
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

      # Retrieves the JMSQueueControl implenetation for current
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
