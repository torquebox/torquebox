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

require 'torquebox/messaging/helpers'

module TorqueBox
  module Messaging
    # Represents a context to the message broker.
    #
    # You should only need to create a Context directly if you are
    # connecting to a remote broker, you need transactional semantics,
    # or you need better perfomance when doing lots of
    # {Destination#publish} or {Destination#receive} calls in rapid
    # succession, since each call will
    # create and close a context if one is not provided.
    class Context
      include TorqueBox::OptionUtils
      include TorqueBox::Messaging::Helpers
      extend TorqueBox::OptionUtils

      attr_reader :internal_context

      # Valid options for Context creation.
      CONTEXT_OPTIONS = optset(WBMessaging::CreateContextOption)

      # Creates a new context.
      #
      # You are responsible for closing any contexts you create.
      #
      # If given a block, the Context instance will be passed to
      # the block and the Context will be closed once the block
      # returns.
      #
      # @param options [Hash]
      # @option options :mode [Symbol] (:auto_ack) One of: :auto_ack,
      #   :client_ack, :transacted
      # @option options :client_id [String] Identifies the client id
      #   for use with a durable topic subscriber.
      # @option options :host [String] The host of a remote broker.
      # @option options :port [Number] (nil, 5445 if :host provided)
      #   The port of a remote broker.
      # @option options :username [String] The username for the remote
      #   broker.
      # @option options :password [String] The password for the remote
      #   broker.
      # @option options :remote_type [Symbol] (:hornetq_standalone)
      #   When connecting to a HornetQ instance running inside
      #   WildFly, this needs to be set to :hornetq_wildfly.
      # @option options :reconnect_attempts [Number] (0) Total number
      #   of reconnect attempts to make before giving up (-1 for unlimited).
      # @option options :reconnect_retry_interval [Number] (2000) The
      #   period in milliseconds between subsequent recontext attempts.
      # @option options :reconnect_max_retry_interval [Number] (2000)
      #   The max retry interval that will be used.
      # @option options :reconnect_retry_interval_multiplier [Number]
      #   (1.0) A multiplier to apply to the time since the last retry
      #   to compute the time to the next retry.
      # @return [Context]
      def initialize(options = {}, &block)
        validate_options(options, CONTEXT_OPTIONS)
        options[:mode] = coerce_mode(options[:mode])
        create_options = extract_options(options, WBMessaging::CreateContextOption)
        @internal_context = default_broker.create_context(create_options)
        if block
          begin
            block.call(self)
          ensure
            close
          end
        end
      end

      # Creates a Queue from this context.
      #
      # The Queue instance will use this context for
      # any of its methods that take a `:context` option.
      #
      # @param (see Queue#initialize)
      # @return (see Queue#initialize)
      def queue(name, options = {})
        TorqueBox::Messaging.queue(name, options.merge(:context => self))
      end

      # Creates a Topic from this context.
      #
      # The Topic instance will use this context for
      # any of its methods that take a `:context` option.
      #
      # @param (see Topic#initialize)
      # @return (see Topic#initialize)
      def topic(name, options = {})
        TorqueBox::Messaging.topic(name, options.merge(:context => self))
      end

      # Closes this context.
      #
      # Once closed, any operations on this context will raise
      # errors.
      # @return [void]
      def close
        @internal_context.close
      end


      # Rolls back the context.
      #
      # This only has affect for :transacted contexts.
      #
      # @return [void]
      def rollback
        @internal_context.rollback
      end

      # Commits the context.
      #
      # This only has affect for :transacted contexts.
      #
      # @return [void]
      def commit
        @internal_context.commit
      end

      # Acknowledge any un-acknowledged messages in this context.
      #
      # This only has affect for :client_ack contexts.
      #
      # @return [void]
      def acknowledge
        @internal_context.acknowledge
      end

      # @return [Symbol] The mode of this context.
      def mode
        case @internal_context.mode
        when WBContext::Mode::AUTO_ACK
          :auto_ack
        when WBContext::Mode::CLIENT_ACK
          :client_ack
        when WBContext::Mode::TRANSACTED
          :transacted
        end
      end

      protected

      def coerce_mode(mode)
        case mode
        when nil
          nil
        when :auto_ack
          WBContext::Mode::AUTO_ACK
        when :client_ack
          WBContext::Mode::CLIENT_ACK
        when :transacted
          WBContext::Mode::TRANSACTED
        else
          fail ArgumentError.new("#{mode} is not a valid context mode.")
        end
      end

    end
  end
end
