module TorqueBox
  module Messaging
    # Represents a messaging topic.
    class Topic < Destination

      # Valid options for topic creation.
      TOPIC_OPTIONS = optset(WBMessaging::CreateTopicOption, :default_options)

      # Creates a new topic reference.
      #
      # This may be a reference to a remote or local (in-vm) topic.
      # Obtaining a reference to an in-vm topic will cause the topic
      # to be created within the broker if it does not already exist.
      # For remote topics, the topic must already exist in the remote
      # broker.
      #
      # If a connection is provided, it will be remembered and
      # used by any method that takes a `:connection` option.
      #
      # @param name [String] The name of the topic.
      # @param options [Hash] Options for topic creation.
      # @option options :connection [Connection] A connection to a
      #   remote broker to use; caller expected to close.
      # @option options :default_options [Hash] A set of default
      #   options to apply to any operations on this topic.
      # @return [Topic] The topic reference.
      def initialize(name, options={})
        validate_options(options, TOPIC_OPTIONS)
        coerced_opts = coerce_connection_and_session(options)
        create_options = extract_options(coerced_opts, WBMessaging::CreateTopicOption)
        super(default_broker.find_or_create_topic(name, create_options),
              options)
      end

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
      #   base {Message} object is passed.
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
    end
  end
end
