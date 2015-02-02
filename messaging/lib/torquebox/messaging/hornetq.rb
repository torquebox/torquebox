# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
      #     destination  type          controller
      #     -------------------------------------------------------------------------
      #     Queue        :jms          org.hornetq.api.jms.management.JMSQueueControl
      #     Queue        :core         org.hornetq.api.core.management.QueueControl
      #     Topic        <ignored>     org.hornetq.api.jms.management.TopicControl
      #
      # Refer to the javadocs for those control classes for details on the
      # available operations.
      # @param destination [Destination] Should be a {Queue} or
      #   {Topic}.
      # @param type [:core, :jms]
      def self.destination_controller(destination, type = :jms)
        if destination.instance_of?(Queue) && type == :core
          prefix = "core.queue."
        else
          prefix = ""
        end
        server_manager
          .getHornetQServer
          .getManagementService
          .getResource("#{prefix}#{jms_name(destination)}")
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
        unless default_broker.respond_to?(:jms_server_manager)
          fail RuntimeError.new("The current broker isn't a HornetQ broker")
        end
        default_broker.jms_server_manager
      end
    end
  end
end
