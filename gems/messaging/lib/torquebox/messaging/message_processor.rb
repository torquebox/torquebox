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

require 'torquebox/messaging/processor_middleware/default_middleware'

module TorqueBox
  module Messaging
    class MessageProcessor
      include ProcessorMiddleware::DefaultMiddleware
      
      attr_accessor :message

      def initialize
        @message = nil
        @proxy = nil
      end

      def initialize_proxy(group)
        @proxy = MessageProcessorProxy.new(group)
      end

      def method_missing(method, *args, &block)
        @proxy.send( method, *args, &block )
      end

      def on_message(body)
        throw "Your subclass must implement on_message(body)"
      end

      def on_error(error)
        raise error
      end

      def process!(message)
        @message = message
        begin
          value = on_message(message.decode)
          reply(value) if synchronous?
        rescue Exception => e
          on_error( e ) 
        end 
      end

      def reply(value)
        TorqueBox::Messaging::Queue.new(@message.jms_message.jms_destination.queue_name).publish(value, :correlation_id => @message.jms_message.jms_message_id)
      end

      class << self

        # List all available message processors for current application.
        #
        # @return [Array<TorqueBox::Messaging::MessageProcessorProxy>] List of
        #         proxy objets to read and manage state of selected message
        #         processor
        def list
          processors = []

          TorqueBox::MSC.get_services(/^#{messaging_service_name.canonical_name}\.\".*\"$/) do |service|
            processors << MessageProcessorProxy.new(service.value)
          end

          processors
        end

        # Lookup a message processor by its destination and class name.
        #
        # @param [String] The destination name (queue, topic) to which
        #         a message processor is bound.
        #
        # @param [String] The class name of the message processor
        #         implementation.
        def lookup(destination_name, class_name)
          sn = messaging_service_name.append("#{destination_name}.#{class_name}")

          # Try to find a message procesor for specified parameters
          group = TorqueBox::ServiceRegistry::lookup(sn)

          return MessageProcessorProxy.new(group) if group

          # Ooops, no processor is found. Most probably wrong data.
          return nil
        end

        protected

        def messaging_service_name
          TorqueBox::MSC.deployment_unit.service_name.append('torquebox').append('messaging')
        end
      end
    end

    class MessageProcessorProxy
      def initialize(group)
        @group = group

        raise "Cannot create MessageProcessorProxy for non-existing MessageProcessorGroup" if @group.nil?
      end

      attr_reader :destination_name, :class_name

      # Updates the concurrency,
      #
      # @note This method sets the concurrency and changes immediately
      #       the number of consumers for specified destination.
      def concurrency=(size)
        raise "Setting concurrency for '#{name}' to value < 0 is not allowed. You tried '#{size}'." if size < 0

        return size if size == @group.concurrency

        @group.update_concurrency(size)

        concurrency
      end

      # Returns the concurrency
      #
      # @return Integer
      def concurrency
        @group.concurrency
      end

      # Returns the group name
      #
      # @return String
      def name
        @group.name
      end

      # Returns the destination (queue or topic) name
      #
      # @return String
      def destination_name
        @group.destination_name
      end

      # Returns the message processor implementation
      # class name
      #
      # @return String
      def class_name
        @group.message_processor_class.name
      end

      # Returns the message selector
      #
      # If there is no message selector specified,
      # returns empty string
      #
      # @return String
      def message_selector
        @group.message_selector
      end

      # Returns true if the message processor is a durable
      # subscriber, false otherwise
      #
      # @return Boolean
      def durable?
        @group.durable
      end

      # Returns true if the message processor is synchronous,
      # false otherwise
      #
      # @return Boolean
      def synchronous?
        @group.synchronous
      end

      def to_s
        "[MessageProcessorProxy: #{name}]"
      end
    end
  end
end
