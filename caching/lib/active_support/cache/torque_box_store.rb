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

require 'active_support/cache'
require 'torquebox/caching'

# @api private
module ActiveSupport
  module Cache
    # @api public
    class TorqueBoxStore < Store

      def initialize(options = {})
        if (ttl = options.delete(:expires_in))
          options[:ttl] = ttl.in_milliseconds
        end
        @name = options.delete(:name) || '__torquebox_store__'
        super(options)
        cache
      end

      # Clear the entire cache. Be careful with this method since it could
      # affect other processes if shared cache is being used.
      def clear(_options = nil)
        cache.clear
      end

      # Delete all entries with keys matching the pattern.
      def delete_matched(matcher, options = nil)
        options = merged_options(options)
        pattern = key_matcher(matcher, options)
        keys.each { |key| delete(key, options) if key =~ pattern }
      end

      # Increment an integer value in the cache; return new value
      def increment(name, amount = 1, options = nil)
        options = merged_options(options)

        # Get the current entry
        key = if respond_to?(:normalize_key, true)
          normalize_key(name, options)
        elsif respond_to?(:namespaced_key, true)
          namespaced_key(name, options)
        end

        current = read_entry(key, options)
        value = current.value.to_i

        new_entry = Entry.new(value + amount, options)
        if cache.compare_and_set(key, current, new_entry)
          return new_entry.value
        else
          raise "Concurrent modification, old value was #{value}"
        end
      end

      # Decrement an integer value in the cache; return new value
      def decrement(name, amount = 1, options = nil)
        increment(name, -amount, options)
      end

      # Cleanup the cache by removing expired entries.
      def cleanup(options = nil)
        options = merged_options(options)
        keys.each do |key|
          entry = read_entry(key, options)
          delete_entry(key, options) if entry && entry.expired?
        end
      end

      protected

      def defaults
        { :mode => :invalidation_async }
      end

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.keys
      end

      # Read an entry from the cache implementation. Subclasses must implement this method.
      def read_entry(key, _options)
        cache.get(key)
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      def write_entry(key, entry, options = {})
        options[:unless_exist] ? cache.put_if_absent(key, entry) : cache.put(key, entry)
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, _options) # :nodoc:
        cache.remove(key) && true
      end


      private

      def cache
        @cache ||= TorqueBox::Caching.cache(@name, defaults.merge(options))
      end
    end
  end
end
