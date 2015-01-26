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

require 'forwardable'

module TorqueBox
  module Messaging
    module HornetQ
      # Sets the HornetQ-specific address options for the given match.
      #
      # This provides programatic access to options that are normally set
      # in the xml configuration.
      class AddressSettings
        include TorqueBox::Messaging::Helpers
        extend Forwardable

        # Creates and registers address settings.
        #
        # Creating a new AddressOptions for a match that you have set
        # options for already will replace those prior options.
        #
        # @param match_or_dest must be either a {Destination} or a fully
        #   qualified jms destination name (prefixed with 'jms.queue.'
        #   or 'jms.topic.'). It may contain HornetQ wildcard matchers
        #   (see
        #   http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/wildcard-syntax.html)
        def initialize(match_or_dest)
          server_manager = HornetQ.server_manager
          import_hornetq

          @address_settings = org.hornetq.core.settings.impl.AddressSettings.new
          server_manager.add_address_settings(normalize_destination_match(match_or_dest), @address_settings)
        end

        # Specifies what should happen when an address reaches
        # max_size_bytes in undelivered messages.
        #
        # See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/paging.html
        #
        # The policy can be one of:
        #
        # * :block - publish calls will block until the current size
        #    drops below max_size_bytes
        # * :drop - new messages are silently dropped
        # * :fail - new messages are dropped and an exception is thrown on publish
        # * :page - new messages will be paged to disk
        #
        # @param policy [Symbol] (:page) The policy to use.
        def address_full_message_policy=(policy)
          policy_const = case policy.to_s
                         when "block"
                           AddressFullMessagePolicy::BLOCK
                         when "drop"
                           AddressFullMessagePolicy::DROP
                         when "fail"
                           AddressFullMessagePolicy::FAIL
                         when "page"
                           AddressFullMessagePolicy::PAGE
                         else
                           fail ArgumentError.new("#{policy} isn't a valid full message policy")
                         end
          @address_settings.address_full_message_policy = policy_const
        end

        # If set, any messages that fail to deliver to their original
        # destination will be delivered here. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/undelivered-messages.html#undelivered-messages.configuring
        #
        # @param address ('jms.queue.DLQ') The address to use. It can
        #   either be a {Destination} object or a fully-qualified
        #   destination name.
        def dead_letter_address=(address)
          @address_settings.dead_letter_address = SimpleString.new(HornetQ.jms_name(address))
        end

        # If set, any messages with a :ttl that expires before
        # delivery will be delivered here. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/message-expiry.html#message-expiry.configuring
        #
        # @param address ('jms.queue.ExpiryQueue') The address to use. It can
        #   either be a {Destination} object or a fully-qualified
        #   destination name.
        def expiry_address=(address)
          @address_settings.expiry_address = SimpleString.new(HornetQ.jms_name(address))
        end

        # If true, only the most recent message
        # for a last-value property will be retained.
        #
        # Setting this option will also cause
        # {#address_full_message_policy=} to be set to :drop, as HornetQ
        # has a bug related to paging last value queues. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/last-value-queues.html
        #
        # @param val [true,false] (false)
        def last_value_queue=(val)
          @address_settings.last_value_queue = val
          self.address_full_message_policy = :drop
        end

        # @!method send_to_dla_on_no_route=(val)
        #
        # If true, any message that can't be routed to its destination
        # will be sent to the {#dead_letter_address=}.
        #
        # @param val [true,false] (false)
        def_delegator(:@address_settings, :setSendToDLAOnNoRoute, :send_to_dla_on_no_route=)

        # @!method expiry_delay=(delay)
        #
        # If > -1, this value (in millis) is used as the
        # default :ttl for messages that don't have a :ttl > 0 set.
        #
        # @param delay [Number] (-1) The delay.
        def_delegators(:@address_settings, :expiry_delay=)

        # @!method max_delivery_attempts=(attempts)
        #
        # The number of times delivery will be attempted for a message
        # before giving up.

        # If {#dead_letter_address=} is set, the message will be
        # delivered there, or removed otherwise. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/undelivered-messages.html#undelivered-messages.configuring
        #
        # @param attempts [Number] (10)
        def_delegators(:@address_settings, :max_delivery_attempts=)

        # @!method max_redelivery_delay=(delay)
        #
        # Specifies the maximum redelivery delay (in millis) when a
        # {#redelivery_multiplier=} is used.
        #
        # @param delay [Number] (same as {#redelivery_delay=})
        def_delegators(:@address_settings, :max_redelivery_delay=)

        # @!method max_size_bytes=(bytes)
        #
        # The maximum size (in bytes) of retained messages
        # on an address before {#address_full_message_policy=} is applied. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/paging.html
        #
        # @param bytes [Number] (20_971_520 (20MB))
        def_delegators(:@address_settings, :max_size_bytes=)

        # @!method page_cache_max_size=(size)
        #
        # HornetQ will keep up to this many page files in memory to
        # optimize IO. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/paging.html
        #
        # @param size [Number] (5)
        def_delegators(:@address_settings, :page_cache_max_size=)

        # @!method page_size_bytes=(bytes)
        #
        # The size (in bytes) of the page files created when
        # paging. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/paging.html
        #
        # @param bytes [Number] (10_485_760 (10MB))
        def_delegators(:@address_settings, :page_size_bytes=)

        # @!method redelivery_delay=(delay)
        #
        # Specifies the delay (in millis) between redelivery
        # attempts. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/undelivered-messages.html#undelivered-messages.delay
        #
        # @param delay [Number] (0)
        def_delegators(:@address_settings, :redelivery_delay=)

        # @!method redelivery_multiplier=(multiplier)
        #
        # Controls the backoff for redeliveries. The
        # delay between redelivery attempts is calculated as
        # {#redelivery_delay=} * ({#redelivery_multiplier=} ^ attempt_count). This won't have
        # any effect if you don't also set {#redelivery_delay=} and {#max_redelivery_delay=}.
        #
        # @param multiplier [Float] (1.0)
        def_delegators(:@address_settings, :redelivery_multiplier=)

        # @!method redistribution_delay=(delay)
        #
        # Specifies the delay (in millis) to wait before
        # redistributing messages from a node in a cluster to other
        # nodes when the queue no longer has consumers on the current
        # node. See
        # http://docs.jboss.org/hornetq/2.3.0.Final/docs/user-manual/html/clusters.html
        #
        # @param delay [Number] (1_000)
        def_delegators(:@address_settings, :redistribution_delay=)

        protected

        def normalize_destination_match(match_or_dest)
          if (match = HornetQ.jms_name(match_or_dest)) != match_or_dest
            match
          elsif match_or_dest == "#" ||
              /^jms\.(queue|topic)\./ =~ match_or_dest
            match_or_dest
          else
            error_message = "#{match_or_dest} isn't a valid match. See the "\
                            "docs for TorqueBox::Messaging::HornetQ::AddressOptions"
            fail ArgumentError.new(error_message)
          end
        end

        def import_hornetq
          java_import org.hornetq.core.settings.impl.AddressFullMessagePolicy
          java_import org.hornetq.api.core.SimpleString
        end
      end
    end
  end
end
