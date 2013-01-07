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
require 'torquebox/kernel'
require 'cache'

# @api private
module ActiveSupport
  module Cache
    # @api public
    class TorqueBoxStore < Store

      SECONDS = java.util.concurrent.TimeUnit::SECONDS

      def initialize(options = {})
        super(options)
        cache
      end

      def name
        options[:name] || TORQUEBOX_APP_NAME
      end

      def clustering_mode
        cache.clustering_mode
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
        current = cache.get(key)
        value = decode(current).value.to_i

        new_entry = Entry.new( value+amount, options )
        if cache.replace( key, current, encode(new_entry) )
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
        {:name=>'__torque_box_store__', :mode => :invalidation, :sync => false}
      end

      def encode value
        Marshal.dump(value).to_java_bytes
      end

      def decode value
        value && Marshal.load(String.from_java_bytes(value))
      end
      
      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.keys
      end

      # Read an entry from the cache implementation. Subclasses must implement this method.
      def read_entry(key, options)
        decode( cache.get( key ) )
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      def write_entry(key, entry, options = {})
        options[:unless_exist] ? cache.put_if_absent( key, encode(entry) ) : cache.put( key, encode(entry) )
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, options) # :nodoc:
        cache.remove( key ) && true
      end


      private

      def cache
        @cache ||= TorqueBox::Infinispan::Cache.new( defaults.merge(options) )
      end
    end
  end
end

