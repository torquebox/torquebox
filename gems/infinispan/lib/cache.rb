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

require 'torquebox/kernel'

module TorqueBox
  module Infinispan

    class Sequence
      def initialize(amount = 1) 
        @data = amount
      end

      def value
        @data ? @data.to_i : @data
      end

      def next(amount = 1)
        @data.to_i + amount
      end

      def ==(other)
        self.value == other.value
      end
    end

    class Cache

      SECONDS = java.util.concurrent.TimeUnit::SECONDS

      def initialize(opts = {})
        @options = opts
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
      def clear
        cache.clearAsync
      end

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.key_set
      end

      def contains_key?( key )
        cache.contains_key( key )
      end

      # Get an entry from the cache 
      def get(key)
        decode(cache.get(key))
      end

      # Write an entry to the cache 
      def put(key, value, expires = 0)
        __put(key, value, expires, :put_async)
      end

      def put_if_absent(key, value, expires = 0)
        __put(key, value, expires, :put_if_absent_async)
      end

      def replace(key, original_value, new_value)
        current = cache.get(key)
        if decode(current) == original_value
          cache.replace( key, current, encode(new_value) )
        end
      end

      # Delete an entry from the cache 
      def remove(key)
        cache.removeAsync( key ) && true
      end

      def increment( sequence_name, amount = 1 )
        current_entry = get( sequence_name )

        # If we can't find the sequence in the cache, create a new one and return
        put( sequence_name, Sequence.new(amount) ) and return amount if current_entry.nil?

        # Increment the sequence, stash it, and return
        new_value = current_entry.next( amount )
        if replace( sequence_name, current_entry, Sequence.new(new_value) )  
          return new_value
        else
          raise "Concurrent modification, old value was #{current_entry.value} new value #{new_value}"
        end
      end

      # Decrement an integer value in the cache; return new value
      def decrement(name, amount = 1)
        increment( name, -amount )
      end

      def ispan_cache
        @cache
      end

      private

      def encode(value)
        value.is_a?(java.lang.Object) ? value : Marshal.dump(value).to_java_bytes
      end

      def decode(value)
        value && (value.is_a?(java.lang.Object) ? value : Marshal.load(String.from_java_bytes(value)))
      end

      def options 
        @options ||= {}
      end

      def cache
        @cache ||= clustered || local || nothing
      end

      def manager
        # TODO: This name should be ServiceName.JBOSS.append("infinispan", "web")
        @manager ||= TorqueBox::ServiceRegistry.lookup("CacheContainerRegistry").cache_container( 'web' ) rescue nil
      end
                       
      def reconfigure(mode=clustering_mode)
        cache = manager.get_cache(name)
        config = cache.configuration
        unless config.cache_mode == mode
          puts "Reconfiguring cache #{name} from #{config.cache_mode} to #{mode}"
          cache.stop
          config.cache_mode = mode
          manager.define_configuration(name, config)
          cache.start
        end
        return cache
      end

      def configure(mode=clustering_mode)
        puts "Configuring cache #{name} as #{mode}"
        config = manager.default_configuration.clone
        config.cache_mode = mode
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
        config  = org.infinispan.config.Configuration.new.fluent
        if options[:persist]
          store = org.infinispan.loaders.file.FileCacheStoreConfig.new
          store.purge_on_startup( false )
          store.location(options[:persist]) if File.exist?( options[:persist].to_s ) 
          config.loaders.add_cache_loader( store )
          config.indexing.index_local_only(true).add_property('indexing', 'in memory')
        end
        manager = org.infinispan.manager.DefaultCacheManager.new(config.build)
        manager.get_cache()
      rescue Exception => e
        puts "Unable to obtain local cache: #{$!}"
        puts e.backtrace
      end
      
      def nothing
        result = Object.new
        def result.method_missing(*args); end
        puts "No caching will occur" 
        result
      end

      def __put(key, value, expires, operation)
        args = [ operation, key, encode(value) ]
        if expires > 0
          # Set the Infinispan expire a few minutes into the future to support
          # :race_condition_ttl on read
          expires_in = expires + 5.minutes
          args << expires_in << SECONDS
        end
        cache.send( *args ) && true
      end
    end

  end
end


