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
    class Topic < Destination

      DEFAULT_SUBSCRIBER_NAME = 'subscriber-1'

      class << self

        # Creates the topic, starts and return a Topic object.
        #
        # @param name The name of the topic
        # @param options Optional parameters (a Hash), including:
        # @option options [Boolean] :exported If the topic should be visible in remote JNDI lookups
        # @return [Topic] if the service is created and started
        # @return [nil] if the service is not created in the specified time (30 s)
        def start(name, options={})
          exported = options.fetch(:exported, false)

          with_destinationizer do |destinationizer|
            latch = destinationizer.create_topic(name, exported)
            return nil unless TorqueBox::Messaging::Destination.wait_for_latch(latch)
          end

          new(name, options)
        end
      end

      # Unsubscribes the selected subscriber from the topic
      #
      # @param subscriber_name The subscriber name to unsubscribe
      # @param options Optional parameters (a Hash)
      # @return [void]
      def unsubscribe(subscriber_name = DEFAULT_SUBSCRIBER_NAME, options = { })
        wait_for_destination(options[:startup_timeout]) do
          with_session do |session|
            session.unsubscribe( subscriber_name )
          end
        end
      end
      
      def to_s
        "[Topic: #{super}]"
      end
    end
  end
end
