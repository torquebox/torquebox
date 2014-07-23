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


module TorqueBox
  module Messaging
    # Represents a messaging topic.
    #
    # Obtain a topic object by calling {TorqueBox::Messaging.topic}.
    class Topic < Destination

      # Valid options for topic creation.
      TOPIC_OPTIONS = optset(WBMessaging::CreateTopicOption, :default_options)

      # Valid options for {#subscribe}.
      SUBSCRIBE_OPTIONS = optset(WBTopic::SubscribeOption)

      # Sets up a durable subscription to this topic, and registers a
      # listener with the given block to receive messages on the
      # subscription.
      #
      # A name is used to identify the subscription, allowing you to
      # stop the listener and resubscribe with the same name in the
      # future without losing messages sent in the interim.
      #
      # If a selector is provided, then only messages having
      # properties matching that expression may be received.
      #
      # If no connection is provided, a new connection is created for this
      # subscriber. If a connection is provided, it must have its :client_id
      # set (see Connection).
      #
      # Subscriptions should be torn down when no longer needed - (see
      # #unsubscribe).

      # @param name [String] The name of the subscription.
      # @param options [Hash] Options for the subscription.
      # @option options :decode [true, false] If true, the decoded
      #   message body is passed to the block. Otherwise, the
      #   base message object is passed.
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close.
      # @return A listener object that can be stopped by
      #   calling .close on it.
      def subscribe(name, options={}, &block)
        validate_options(options, SUBSCRIBE_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        handler = MessageHandler.new do |message|
          block.call(options.fetch(:decode, true) ? message.body : message)
        end
        @internal_destination.subscribe(name, handler,
                                        Codecs.java_codecs,
                                        extract_options(options, WBTopic::SubscribeOption))
      end

      # Valid options for {#unsubscribe}.
      UNSUBSCRIBE_OPTIONS = optset(WBTopic::UnsubscribeOption)

      # Tears down a durable topic subscription.
      #
      # If no connection is provided, a new connection is created for
      # this action. If a connection is provided, it must have its
      # :client_id set to the same value used when creating the
      # subscription (see #subscribe).
      #
      # @param name [String] The name of the subscription.
      # @param options [Hash] Options for the subscription.
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close.
      # @return [void]
      def unsubscribe(name, options={})
        validate_options(options, UNSUBSCRIBE_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        @internal_destination.unsubscribe(name,
                                          extract_options(options, WBTopic::UnsubscribeOption))
      end

      protected

      def initialize(name, options={})
        validate_options(options, TOPIC_OPTIONS)
        coerced_opts = coerce_connection_and_session(options)
        create_options = extract_options(coerced_opts, WBMessaging::CreateTopicOption)
        super(default_broker.find_or_create_topic(name, create_options),
              options)
      end

    end
  end
end
