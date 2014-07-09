# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

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
      def clear(options = nil)
        cache.clear
      end

      # Delete all entries with keys matching the pattern.
      def delete_matched( matcher, options = nil )
        options = merged_options(options)
        pattern = key_matcher( matcher, options )
        keys.each { |key| delete( key, options ) if key =~ pattern }
      end

      # Increment an integer value in the cache; return new value
      def increment(name, amount = 1, options = nil)
        options = merged_options( options )

        # Get the current entry
        key = namespaced_key( name, options )
        current = read_entry(key, options)
        value = current.value.to_i

        new_entry = Entry.new( value+amount, options )
        m = cache.java_method(:replace, [java.lang.Object, java.lang.Object, java.lang.Object])
        if m.call(key, current, new_entry)
          return new_entry.value
        else
          raise "Concurrent modification, old value was #{value}"
        end
      end

      # Decrement an integer value in the cache; return new value
      def decrement(name, amount = 1, options = nil)
        increment( name, -amount, options )
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
        {:mode => :invalidation_async}
      end

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.keys
      end

      # Read an entry from the cache implementation. Subclasses must implement this method.
      def read_entry(key, options)
        cache.get( key )
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      def write_entry(key, entry, options = {})
        previous_value = options[:unless_exist] ? cache.put_if_absent( key, entry ) : cache.put( key, entry )
        previous_value unless previous_value.nil?
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, options) # :nodoc:
        cache.remove( key ) && true
      end


      private

      def cache
        @cache ||= TorqueBox::Caching.cache(@name, defaults.merge(options))
      end
    end
  end
end
