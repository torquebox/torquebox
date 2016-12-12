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
      PUBLISH_OPTIONS = optset(WBDestination::PublishOption, :encoding)

      # Send a message to this destination.
      #
      # The message can be any data that can be encoded using the
      # given :encoding.
      #
      # If no context is provided, a new context will be created, then
      # closed.
      #
      # @param message [Object] The message to send.
      # @param options [Hash] Options for message publication.
      # @option options :encoding [Symbol] (:marshal) one of: :edn, :json,
      #   :marshal, :marshal_base64, :text
      # @option options :priority [Symbol, Number] (:normal) 0-9, or
      #   one of: :low, :normal, :high, :critical
      # @option options :ttl [Number] (0) time to live, in millis, 0
      #   == forever
      # @option options :persistent [true, false] (true) whether
      #   undelivered messages survive restarts
      # @option options :properties [Hash] a hash to which selectors
      #   may be applied
      # @option options :context [Context] a context to use;
      #   caller expected to close
      # @return [void]
      def publish(message, options = {})
        validate_options(options, PUBLISH_OPTIONS)
        options = apply_default_options(options)
        options = normalize_publish_options(options)
        options = coerce_context(options)
        encoding = options[:encoding] || Messaging.default_encoding
        @internal_destination.publish(message,
                                      Codecs[encoding],
                                      extract_options(options, WBDestination::PublishOption))
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
      # If no context is provided, a new context will be created, then
      # closed.
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
      #   base message object is returned.
      # @option options :context [Context] a context to use;
      #   caller expected to close
      # @return The message, or the return value of the block if a
      #   block is given.
      def receive(options = {}, &block)
        validate_options(options, RECEIVE_OPTIONS)
        options = apply_default_options(options)
        options = coerce_context(options)
        result = @internal_destination.receive(Codecs.java_codecs,
                                               extract_options(options, WBDestination::ReceiveOption))
        msg = if result
                options.fetch(:decode, true) ? result.body : result
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
      # If given a context, the context must be remote, and the mode
      # of that context is ignored, since it is used solely to
      # generate sub-contexts for each listener thread. Closing the
      # given context will also close the listener.
      #
      # If no context is provided, a new context will be created, then
      # closed.
      #
      # @param options [Hash] Options for the listener.
      # @option options :concurrency [Number] (1) The number of
      #   threads handling messages.
      # @option options :selector [String] A JMS (SQL 92) expression
      #   matching message properties
      # @option options :decode [true, false] If true, the decoded
      #   message body is passed to the block. Otherwise, the
      #   base message object is passed.
      # @option options :context [Context] a *remote* context to
      #   use; caller expected to close.
      # @return A listener object that can be stopped by
      #   calling .close on it.
      def listen(options = {}, &block)
        validate_options(options, LISTEN_OPTIONS)
        options = apply_default_options(options)
        options = coerce_context(options)
        handler = MessageHandler.new do |message|
          block.call(options.fetch(:decode, true) ? message.body : message)
          nil
        end
        @internal_destination.listen(handler,
                                     Codecs.java_codecs,
                                     extract_options(options, WBDestination::ListenOption))
      end

      # Stops this destination.
      #
      # Note that stopping a destination may remove it from the broker if
      # called outside of the container.
      #
      # @return [void]
      def stop
        @internal_destination.stop
      end

      protected

      def initialize(internal_destination, options)
        @internal_destination = internal_destination
        @default_options = options[:default_options] || {}
        @default_options[:context] ||= options[:context]
      end

      def apply_default_options(options)
        @default_options.merge(options || {})
      end

      def coerce_context(options)
        options = options.dup
        options[:context] = options[:context].internal_context if options[:context]
        options
      end

      def normalize_publish_options(options)
        options = options.dup
        if options.key?(:priority)
          if PRIORITY_MAP[options[:priority]]
            options[:priority] = PRIORITY_MAP[options[:priority]]
          elsif (0..9) === options[:priority].to_i
            options[:priority] = options[:priority].to_i
          else
            priorities = PRIORITY_MAP.keys.map { |k| ":#{k}" }.join(',')
            fail ArgumentError.new(":priority must in the range 0..9, or one of #{priorities}")
          end
        end
        options
      end

      # @api private
      class MessageHandler
        include Java::OrgProjectoddWunderbossMessaging::MessageHandler

        def initialize(&block)
          @block = block
        end

        def on_message(message, context)
          @block.call(message, context)
        end
      end

    end
  end
end
