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
