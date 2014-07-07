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

java_import java.util.concurrent.TimeUnit
java_import org.projectodd.wunderboss.messaging.ConcreteReply

module TorqueBox
  module Messaging
    # Represents a messaging queue.
    class Queue < Destination

      # Valid options for Queue creation.
      QUEUE_OPTIONS = optset(WBMessaging::CreateQueueOption, :default_options)

      # Creates a new queue reference.
      #
      # This may be a reference to a remote or local (in-vm) queue.
      # Obtaining a reference to an in-vm queue will cause the queue
      # to be created within the broker if it does not already exist.
      # For remote queues, the queue must already exist in the remote
      # broker.
      #
      # If a connection is provided, it will be remembered and
      # used by any method that takes a `:connection` option.
      #
      # @param name [String] The name of the queue.
      # @param options [Hash] Options for queue creation.
      # @option options :connection [Connection] A connection to a
      #   remote broker to use; caller expected to close.
      # @option options :durable [true, false] (true) Whether messages
      #   persist across restarts.
      # @option options :selector [String] A JMS (SQL 92) expression
      #   to filter published messages.
      # @option options :default_options [Hash] A set of default
      #   options to apply to any operations on this queue.
      # @return [Queue] The queue reference.
      def initialize(name, options={})
        validate_options(options, QUEUE_OPTIONS)
        coerced_opts = coerce_connection_and_session(options)
        create_options = extract_options(coerced_opts, WBMessaging::CreateQueueOption)
        super(default_broker.find_or_create_queue(name, create_options),
              options)
      end

      # Valid options for {#request}.
      REQUEST_OPTIONS = optset(PUBLISH_OPTIONS, :timeout, :timeout_val)

      # Sends a message this queue and waits for a response.
      #
      # Implements the request-response pattern, and is used in
      # conjunction with {#respond}.
      #
      # Can optionally be given block that will be called with
      # the message.
      #
      # If no connection is provided, the default shared connection is
      # used.
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
      # @option options :timeout [Number] (0) Time in millis,
      #   after which the :timeout_val is returned. 0
      #   means wait forever.
      # @option options :timeout_val [Object] The value to
      #   return when a timeout occurs.
      # @return The message, or the return value of the block if a
      #   block is given.
      def request(message, options={}, &block)
        validate_options(options, REQUEST_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        options = normalize_publish_options(options)
        encoding = options[:encoding] || Messaging.default_encoding
        future = @internal_destination.request(message,
                                               Codecs[encoding],
                                               Codecs.java_codecs,
                                               extract_options(options, WBQueue::RequestOption))
        timeout = options[:timeout] || 0
        result = if timeout == 0
                   future.get
                 else
                   begin
                     future.get(timeout, TimeUnit::MILLISECONDS)
                   rescue java.util.concurrent.TimeoutException
                     nil
                   end
                 end
        msg = if result
                result.body
              else
                options[:timeout_val]
              end
        if block
          block.call(msg)
        else
          msg
        end
      end

      # Valid options for {#respond}
      RESPOND_OPTIONS = optset(WBQueue::RespondOption, :decode)

      # Registers a block to receive each request message sent to this
      # destination, and returns the result of the block call to the
      # requestor.
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
      #   base message object is passed.
      # @option options :ttl [Number] (60000) The time for the
      #   response message to live, in millis.
      # @option options :connection [Connection] a connection to use;
      #   caller expected to close
      # @return A listener object that can be stopped by
      #   calling .close on it.
      def respond(options={}, &block)
        validate_options(options, RESPOND_OPTIONS)
        options = apply_default_options(options)
        options = coerce_connection_and_session(options)
        handler = MessageHandler.new do |message|
          ConcreteReply.new(block.call(options.fetch(:decode, true) ? message.body : message),
                            nil)
        end
        @internal_destination.respond(handler,
                                      Codecs.java_codecs,
                                      extract_options(options, WBQueue::RespondOption))
      end

    end
  end
end
