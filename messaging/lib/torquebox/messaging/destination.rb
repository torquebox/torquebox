require 'torquebox/codecs'
require 'torquebox/messaging/helpers'

module TorqueBox
  module Messaging
    # Represents a messaging destination.
    #
    # You'll never instantiate a Destination directly, but use {Queue}
    # or {Topic} instead.
    class Destination
      include TorqueBox::OptionUtils
      include TorqueBox::Messaging::Helpers
      extend TorqueBox::OptionUtils

      attr_accessor :internal_destination

      # Maps priority mnemonics to priority values.
      PRIORITY_MAP = {
        :low => 1,
        :normal => 4,
        :high => 7,
        :critical => 9
      }

      # Valid options for {#publish}.
      PUBLISH_OPTIONS = optset(WBDestination::SendOption, :encoding)

      # Send a message to this destination.
      #
      # The message can be any data that can be encoded using the
      # given :encoding.
      #
      # If no connection is provided, the default shared connection is
      # used. If no session is provided, a new one is opened and closed.
      #
      # @param message [Object] The message to send.
      # @param options [Hash] Options for message publication.
      # @option options :encoding [Symbol] (:marshal) one of: :edn, :json,
      #   :marshal, :marshal, :marshal_base64, :text
      # @option options :priority [Symbol, Number] (:normal) 0-9, or
      #   one of: :low, :normal, :high, :critical
      # @option options :ttl [Number] (0) time to live, in millis, 0
      #   == forever
      # @option options :persistent [true, false] (true) whether
      #   undelivered messages survive restarts
      # @option options :properties [Hash] a hash to which selectors
      #   may be applied
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close
      # @option options :session [Session] a session to use; caller
      #   expected to close
      # @return [void]
      def publish(message, options={})
        validate_options(options, PUBLISH_OPTIONS)
        options = apply_default_options(options)
        options = normalize_publish_options(options)
        options = coerce_connection_and_session(options)
        encoding = options[:encoding] || Messaging.default_encoding
        @internal_destination.send(Codecs.encode(message, encoding),
                                   Codecs.content_type_for_name(encoding),
                                   extract_options(options, WBDestination::SendOption))
      end

      # Valid options for {#receive}.
      RECEIVE_OPTIONS = optset(WBDestination::ReceiveOption, :decode, :timeout_val)

      # Receive a message from this destination.
      #
      # Can optionally be given a block that will be called with
      # the message.
      #
      # If a :selector is provided, then only messages having
      # properties matching that expression may be received.
      #
      # If no connection is provided, the default shared connection is
      # used. If no session is provided, a new one is opened and closed.
      #
      # @param options [Hash] Options for message receipt.
      # @option options :timeout [Number] (10000) Time in millis,
      #   after which the :timeout_val is returned. 0
      #   means wait forever, -1 means don't wait at all
      # @option options :timeout_val [Object] The value to
      #   return when a timeout occurs. Also returned when
      #   a :timeout of -1 is specified, and no message is available
      # @option options :selector [String] A JMS (SQL 92) expression
      #   matching message properties
      # @option options :decode [true, false] (true) If true, the
      #   decoded message body is returned. Otherwise, the
      #   base {Message} object is returned.
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close
      # @option options :session [Session] a session to use; caller
      #   expected to close
      # @return The message, or the return value of the block if a
      #   block is given.
      def receive(options={}, &block)
        validate_options(options, RECEIVE_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        result = @internal_destination.receive(extract_options(options, WBDestination::ReceiveOption))
        msg = if result
                m = Message.new(result)
                options.fetch(:decode, true) ? m.decode : m
              else
                options[:timeout_val]
              end
        block ? block.call(msg) : msg
      end

      # Valid options for {#listen}
      LISTEN_OPTIONS = optset(WBDestination::ListenOption, :encoding, :decode)

      # Registers a block to receive each message sent to this destination.
      #
      # If a :selector is provided, then only messages having
      # properties matching that expression will be received.
      #
      # If no connection is provided, the default shared connection is
      # used.
      #
      # @param options [Hash] Options for the listener.
      # @option options :concurrency [Number] (1) The number of
      #   threads handling messages.
      # @option options :selector [String] A JMS (SQL 92) expression
      #   matching message properties
      # @option options :decode [true, false] If true, the decoded
      #   message body is passed to the block. Otherwise, the
      #   base {Message} object is passed.
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close.
      # @return A listener object that can be stopped by
      #   calling .close on it.
      def listen(options={}, &block)
        validate_options(options, LISTEN_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        handler = MessageHandler.new do |message|
          msg = Message.new(message)
          block.call(options.fetch(:decode, true) ? msg.decode : msg)
        end
        @internal_destination.listen(handler,
                                     extract_options(options, WBDestination::ListenOption))
      end

      # Stops this destination.
      #
      # Note that stopping a destination may remove it from the broker if
      # called outside of the container.
      #
      # @return [void]
      def stop
        @internal_endpoint.stop
      end

      protected

      def initialize(internal_destination, options)
        @internal_destination = internal_destination
        @default_options = options[:default_options] || {}
        @default_options[:connection] ||= options[:connection]
      end

      def apply_default_options(options)
        @default_options.merge(options || {})
      end

      def coerce_connection_and_session(options)
        options = options.dup
        options[:connection] = options[:connection].internal_connection if options[:connection]
        options[:session] = options[:session].internal_session if options[:session]
        options
      end

      def normalize_publish_options(options)
        options = options.dup
        if options.has_key?(:priority)
          if PRIORITY_MAP[options[:priority]]
            options[:priority] = PRIORITY_MAP[options[:priority]]
          elsif (0..9) === options[:priority].to_i
            options[:priority] = options[:priority].to_i
          else
            fail ArgumentError.new(":priority must in the range 0..9, or one of #{PRIORITY_MAP.keys.collect {|k| ":#{k}"}.join(',')}")
          end
        end
        options
      end

      # @api private
      class MessageHandler
        include org.projectodd.wunderboss.messaging.MessageHandler

        def initialize(&block)
          @block = block
        end

        def on_message(message, session)
          @block.call(message, session)
        end
      end

    end
  end
end
