require 'torquebox/messaging/hornetq/address_settings'

module TorqueBox
  module Messaging
    module HornetQ
      extend TorqueBox::Messaging::Helpers

      # Returns the destination controller for the given destination.
      #
      # The returned controller depends on the type of the given
      # destination and, for queues, the requested type:
      #
      # ```
      # destination  type          controller
      # -------------------------------------------------------------------------
      # Queue        :jms          org.hornetq.api.jms.management.JMSQueueControl
      # Queue        :core         org.hornetq.api.core.management.QueueControl
      # Topic        <ignored>     org.hornetq.api.jms.management.TopicControl
      # ```
      #
      # Refer to the javadocs for those control classes for details on the
      # available operations.
      # @param destination [Destination] Should be a {Queue} or
      #   {Topic}.
      # @param type [:core, :jms]
      def self.destination_controller(destination, type = :jms)
        prefix = (destination.instance_of?(Queue) && type == :core ?
                    "core.queue." : "")
        server_manager.
          getHornetQServer.
          getManagementService.
          getResource("#{prefix}#{jms_name(destination)}")
      end

      protected

      def self.jms_name(dest)
        if dest.respond_to?(:internal_destination)
          dest.internal_destination.jms_name
        else
          dest
        end
      end

      def self.server_manager
        fail RuntimeError.new("The current broker isn't a HornetQ broker") if !default_broker.respond_to?(:jms_server_manager)
        default_broker.jms_server_manager
      end
    end
  end
end
