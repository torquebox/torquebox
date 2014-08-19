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

require 'torquebox/messaging/connection'
require 'torquebox/messaging/destination'
require 'torquebox/messaging/queue'
require 'torquebox/messaging/topic'
require 'torquebox/messaging/session'
require 'torquebox/messaging/hornetq'

module TorqueBox
  module Messaging
    def self.default_encoding
      @default_encoding ||= :marshal
    end

    def self.default_encoding=(enc)
      @default_encoding = enc
    end

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
    def self.queue(name, options = {})
      Queue.new(name, options)
    end

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
    def self.topic(name, options = {})
      Topic.new(name, options)
    end

  end
end
