# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

module ActiveSupport
  module Cache
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
        java_import org.infinispan.config.Configuration::CacheMode
        replicated =  [:r, :repl, :replicated, :replication].include? options[:mode]
        distributed = [:d, :dist, :distributed, :distribution].include? options[:mode]
        sync = !!options[:sync]
        case
        when replicated 
          sync ? CacheMode::REPL_SYNC : CacheMode::REPL_ASYNC
        when distributed
          sync ? CacheMode::DIST_SYNC : CacheMode::DIST_ASYNC
        else
          sync ? CacheMode::INVALIDATION_SYNC : CacheMode::INVALIDATION_ASYNC
        end
      end

      # Clear the entire cache. Be careful with this method since it could
      # affect other processes if shared cache is being used.
      def clear(options = nil)
        cache.clearAsync
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

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.key_set
      end

      # Read an entry from the cache implementation. Subclasses must implement this method.
      def read_entry(key, options)
        decode(cache.get(key))
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      def write_entry(key, entry, options = {})
        args = [ :put_async, key, encode(entry) ]
        args[0] = :put_if_absent_async if options[:unless_exist]
        if options[:expires_in]
          # Set the Infinispan expire a few minutes into the future to support
          # :race_condition_ttl on read
          expires_in = options[:expires_in].to_i + 5.minutes
          args << expires_in << SECONDS
        end
        cache.send( *args ) && true
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, options) # :nodoc:
        cache.removeAsync( key ) && true
      end

      def encode value
        Marshal.dump(value).to_java_bytes
      end

      def decode value
        value && Marshal.load(String.from_java_bytes(value))
      end

      private

      def cache
        @cache ||= clustered || local || nothing
      end

      def manager
        @manager ||= TorqueBox::ServiceRegistry.lookup('jboss.infinispan.web' ) rescue nil
      end
                       
      def reconfigure(mode=clustering_mode)
        cache = manager.get_cache(name)
        config = cache.configuration
        unless config.cache_mode == mode
          puts "Reconfiguring cache #{name} from #{config.cache_mode} to #{mode}"
          cache.stop
          config.cache_mode = mode.to_s
          manager.define_configuration(name, config)
          cache.start
        end
        return cache
      end

      def configure(mode=clustering_mode)
        puts "Configuring cache #{name} as #{mode}"
        config = manager.default_configuration.clone
        config.cache_mode = mode.to_s
        manager.define_configuration(name, config)
        manager.get_cache(name)
      end

      def clustered
        if manager.running?(name)
          reconfigure
        else
          configure
        end
      rescue
        puts "Unable to obtain clustered cache; falling back to local: #{$!}" if manager
      end

      def local
        # workaround common problem running infinispan in web containers (see FAQ)
        java.lang.Thread.current_thread.context_class_loader = org.infinispan.Cache.java_class.class_loader
        manager = org.infinispan.manager.DefaultCacheManager.new()
        manager.get_cache()
      rescue
        puts "Unable to obtain local cache: #{$!}"
      end
      
      def nothing
        result = Object.new
        def result.method_missing(*args); end
        logger.warn "No caching will occur" if logger
        result
      end

    end
  end
end

