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

java_import java.util.concurrent::TimeUnit
java_import org.projectodd.wunderboss.caching.notifications::Listener

module TorqueBox
  module Caching
    class Cache
      extend Forwardable

      # Wraps a real Infinispan cache object with a slightly simpler
      # interface. The wrapped cache is available via the {#cache}
      # accessor.
      #
      # @param cache [org.infinispan.Cache] The wrapped cache
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      def initialize(cache, options = {})
        @cache = cache
        @options = options
      end

      # Associate key to value in the cache. Expiration options
      # override any passed to the constructor.
      #
      # @param key [Object]
      # @param value [Object]
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      # @return [Object] the old value, if any
      def put(key, value, options = {})
        put_m.call(*[key, value] + expiry(options))
      end

      # Put a map of entries into the cache. Expiration options
      # override any passed to the constructor.
      #
      # @param map [Hash]
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      # @return [void]
      def put_all(map, options = {})
        putall_m.call(*[map] + expiry(options))
      end

      # Associate key to value only if key doesn't exist in cache.
      # Expiration options override any passed to the constructor.
      #
      # @param key [Object]
      # @param value [Object]
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      # @return [Object] nil on success, otherwise the old value
      def put_if_absent(key, value, options = {})
        putif_m.call(*[key, value] + expiry(options))
      end

      # Associate key to value only if key exists in cache. Expiration
      # options override any passed to the constructor.
      #
      # @param key [Object]
      # @param value [Object]
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      # @return [Object] the old value on success, otherwise nil
      def replace(key, value, options = {})
        replace_m.call(*[key, value] + expiry(options))
      end

      # Associate key to a new value only if it's currently mapped to
      # a specific value. Expiration options override any passed to
      # the constructor.
      #
      # @param key [Object] the key
      # @param old_value [Object] the current value of the key
      # @param new_value [Object] the desired value of the key
      # @param options [Hash] Options for entry expiration
      # @option options :ttl [Number] (-1) milliseconds the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) milliseconds after which an
      #   entry will expire if not accessed
      # @return [true, false] true if value successfully replaced
      def compare_and_set(key, old_value, new_value, options = {})
        cas_m.call(*[key, old_value, new_value] + expiry(options))
      end

      # Clear all entries from the cache
      #
      # @return [void]
      def clear
        @cache.clear
        self
      end

      # Infinispan's cache notifications API is based on Java
      # annotations, which can be awkward in JRuby (and Java, for that
      # matter).
      #
      # This function provides the ability to map one or more symbols
      # to a block that will be passed an
      # {https://docs.jboss.org/infinispan/6.0/apidocs/org/infinispan/notifications/cachelistener/event/package-summary.html
      # Infinispan Event} instance.
      #
      # Each symbol corresponds to an event type, i.e. one of the
      # {http://docs.jboss.org/infinispan/6.0/apidocs/org/infinispan/notifications/cachelistener/annotation/package-summary.html
      # Infinispan annotations}:
      #
      #    :cache_entries_evicted
      #    :cache_entry_activated
      #    :cache_entry_created
      #    :cache_entry_invalidated
      #    :cache_entry_loaded
      #    :cache_entry_modified
      #    :cache_entry_passivated
      #    :cache_entry_removed
      #    :cache_entry_visited
      #    :data_rehashed
      #    :topology_changed
      #    :transaction_completed
      #    :transaction_registered
      #
      # The callbacks are synchronous, i.e. invoked on the thread acting on
      # the cache. For longer running callbacks, use a queue or some sort of
      # asynchronous channel.
      #
      # The return value is an array of listener objects corresponding
      # to the requested event types, which will be a subset of those
      # returned from the {get_listeners} method. These may be passed
      # to the {remove_listener} method to turn off notifications.
      def add_listener(*types, &block)
        handler = Handler.new(block)
        listeners = types.map { |type| Listener::listen(handler, type.to_s) }
        listeners.each { |listener| @cache.add_listener(listener) }
      end

      # @!method get(key)
      #
      # Get the value associated with the key
      #
      # @return [Object] nil if missing
      def_delegators :@cache, :get

      # @!method size()
      #
      # Get the number of entries in the cache
      #
      # @return [Fixnum]
      def_delegators :@cache, :size

      # @!method empty?()
      #
      # Return true if cache contains no entries
      #
      # @return [true, false]
      def_delegators :@cache, :empty?

      # @!method entry_set()
      #
      # Return a Set of Map.Entry instances
      #
      # @return [Set]
      def_delegators :@cache, :entry_set

      # @!method contains_key?(key)
      #
      # Return true if cache contains the key
      #
      # @return [true, false]
      def_delegators :@cache, :contains_key?

      # @!method evict(key)
      #
      # Remove entry from the heap, but not persistent storage, so
      # subsequent reads will cause it to be reloaded
      #
      # @return [void]
      def_delegators :@cache, :evict

      # @!method remove(key)
      #
      # Remove the entry associated with the key
      #
      # @return [Object] the old value or nil, if key is missing
      def_delegators :@cache, :remove

      # @!method values()
      #
      # Get the values in the cache
      #
      # @return [Array]
      def_delegators :@cache, :values

      # @!method keys()
      #
      # Get the keys in the cache
      #
      # @return [Array]
      def_delegators :@cache, :keys

      # @!method name()
      #
      # Get cache name
      #
      # @return [String]
      def_delegators :@cache, :name

      # @!method get_listeners()
      #
      # Get the cache's active event listener instances
      #
      # @return [Array]
      def_delegators :@cache, :get_listeners

      # @!method remove_listener(listener)
      #
      # Turn off a particular event listener
      #
      # @return [void]
      def_delegators :@cache, :remove_listener

      # @!method configuration()
      #
      # Get the cache's configuration instance
      #
      # @return [org.infinispan.configuration.cache.Configuration]
      def_delegator :@cache, :cache_configuration, :configuration

      # @!method cache()
      #
      # Accessor for the wrapped cache instance
      #
      # @return [org.infinispan.Cache]
      attr_accessor :cache

      def_delegators :@cache, :[], :[]=


      private

      def defaults(options)
        { :ttl => -1, :idle => -1 }.merge(@options).merge(options)
      end

      def expiry(options)
        m = defaults(options)
        [m[:ttl], TimeUnit::MILLISECONDS, m[:idle], TimeUnit::MILLISECONDS]
      end

      class Handler
        include Java::OrgProjectoddWunderbossCachingNotifications::Handler
        def initialize(block)
          @block = block
        end

        def handle(event)
          @block.call(event)
        end
      end

      def replace_m
        @replace_m ||= @cache.java_method(:replace, [java.lang.Object,
                                                     java.lang.Object,
                                                     Java::long,
                                                     java.util.concurrent.TimeUnit,
                                                     Java::long,
                                                     java.util.concurrent.TimeUnit])
      end

      def cas_m
        @cas_m ||= @cache.java_method(:replace, [java.lang.Object,
                                                 java.lang.Object,
                                                 java.lang.Object,
                                                 Java::long,
                                                 java.util.concurrent.TimeUnit,
                                                 Java::long,
                                                 java.util.concurrent.TimeUnit])
      end

      def put_m
        @put_m ||= @cache.java_method(:put, [java.lang.Object,
                                             java.lang.Object,
                                             Java::long,
                                             java.util.concurrent.TimeUnit,
                                             Java::long,
                                             java.util.concurrent.TimeUnit])
      end

      def putall_m
        @putall_m ||= @cache.java_method(:putAll, [java.util.Map.java_class,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit])
      end

      def putif_m
        @putif_m ||= @cache.java_method(:putIfAbsent, [java.lang.Object,
                                                       java.lang.Object,
                                                       Java::long,
                                                       java.util.concurrent.TimeUnit,
                                                       Java::long,
                                                       java.util.concurrent.TimeUnit])
      end

    end
  end
end
