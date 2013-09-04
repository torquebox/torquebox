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

require 'torquebox/kernel'
require 'torquebox/injectors'
require 'torquebox/transactions'
require 'torquebox/codecs'

module TorqueBox
  module Infinispan

    # @api private
    class ContainerTransactionManagerLookup
      begin
        include org.infinispan.transaction.lookup.TransactionManagerLookup
      rescue NameError
        # Not running inside TorqueBox
      end

      def getTransactionManager
        TorqueBox.fetch('transaction-manager')
      end
    end

    class Cache

      SECONDS = java.util.concurrent.TimeUnit::SECONDS
      begin
        java_import org.infinispan.configuration.cache::CacheMode
        java_import org.infinispan.configuration.cache::ConfigurationBuilder
        java_import org.infinispan.transaction::TransactionMode
        java_import org.infinispan.transaction::LockingMode
        java_import org.infinispan.eviction::EvictionStrategy
        java_import org.projectodd.polyglot.cache.as::CacheService
        INFINISPAN_AVAILABLE = true
      rescue NameError => e
        INFINISPAN_AVAILABLE = false
        # Not running inside TorqueBox
      end

      def initialize(opts = {})
        @options = opts
        
        if INFINISPAN_AVAILABLE
          options[:transaction_mode] = :transactional unless options.has_key?( :transaction_mode )
          options[:locking_mode] ||= :optimistic if (transactional? && !options.has_key?( :locking_mode ))
          options[:sync] = true if options[:sync].nil?
        end

        if options[:encoding] == :marshal
          log( "Encoding of :marshal cannot be used with " +
               "TorqueBox::Infinispan::Cache - using :marshal_base64 instead",
               'WARN')
          options[:encoding] = :marshal_base64
        end
        @codec = TorqueBox::Codecs[ options[:encoding] || :marshal_smart ]
        cache
      end

      def name
        options[:name] || TORQUEBOX_APP_NAME
      end

      def persisted?
        !!options[:persist]
      end

      def replicated?
        [:r, :repl, :replicated, :replication].include? options[:mode]
      end

      def distributed?
        [:d, :dist, :distributed, :distribution].include? options[:mode]
      end

      def invalidated?
        [:i, :inv, :invalidated, :invalidation].include? options[:mode] 
      end

      def clustered?
        INFINISPAN_AVAILABLE && service.clustered? 
      end

      def clustering_mode
        return CacheMode::LOCAL unless clustered?
        sync = options[:sync]
        case
        when replicated?
          sync ? CacheMode::REPL_SYNC : CacheMode::REPL_ASYNC
        when distributed?
          sync ? CacheMode::DIST_SYNC : CacheMode::DIST_ASYNC
        when invalidated?
          sync ? CacheMode::INVALIDATION_SYNC : CacheMode::INVALIDATION_ASYNC
        else
          sync ? CacheMode::DIST_SYNC : CacheMode::DIST_ASYNC
        end
      end

      def locking_mode
        case options[:locking_mode]
        when :optimistic then LockingMode::OPTIMISTIC
        when :pessimistic then LockingMode::PESSIMISTIC
        end
      end

      def transaction_mode
        options[:transaction_mode] == :transactional ? TransactionMode::TRANSACTIONAL : TransactionMode::NON_TRANSACTIONAL
      end

      def transactional?
        transaction_mode == TransactionMode::TRANSACTIONAL
      end

      def eviction_strategy
        case options[:eviction]
        when :lirs then EvictionStrategy::LIRS
        when :lru then EvictionStrategy::LRU
        when :unordered then EvictionStrategy::UNORDERED
        end
      end

      def max_entries
        options[:max_entries] || -1
      end

      # Clear the entire cache. Be careful with this method since it could
      # affect other processes if shared cache is being used.
      def clear
        cache.clear
      end

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.key_set.map{|k| decode(k)}
      end

      def size
        cache.size
      end

      def all
        keys.map{|k| get(k)}
      end
      alias_method :values, :all

      def contains_key?( key )
        cache.contains_key( encode(key) )
      end

      # Get an entry from the cache 
      def get(key)
        decode(cache.get(encode(key)))
      end
      alias_method :[], :get

      # Write an entry to the cache 
      def put(key, value, expires = 0)
        __put(key, value, expires, :put)
      end
      alias_method :[]=, :put

      def put_if_absent(key, value, expires = 0)
        __put(key, value, expires, :put_if_absent)
      end

      def evict( key )
        cache.evict( encode(key) )
      end

      def replace(key, original_value, new_value)
        cache.replace(encode(key), encode(original_value), encode(new_value))
      end

      # Delete an entry from the cache
      def remove(key)
        decode(cache.remove(encode(key)))
      end

      def increment(sequence_name, amount = 1)
        result, current = amount, get(sequence_name)
        if current.nil?
          put(sequence_name, result)
        else
          result = current + amount
          unless replace(sequence_name, current, result)
            raise "Concurrent modification, old value was #{current} new value #{result}"
          end
        end
        result
      end

      # Decrement an integer value in the cache; return new value
      def decrement(name, amount = 1)
        increment( name, -amount )
      end

      def transaction(&block)
        if !transactional?
          yield self
        elsif TorqueBox.fetch('transaction-manager').nil?
          tm = cache.getAdvancedCache.getTransactionManager
          begin
            tm.begin if tm
            yield self
            tm.commit if tm
          rescue Exception => e
            if tm.nil?
              log( "Transaction is nil", "ERROR" )
              log( e.message, 'ERROR' )
              log( e.backtrace, 'ERROR' )
            elsif tm.status == javax.transaction.Status.STATUS_NO_TRANSACTION
              log( "No transaction was started", "ERROR" )
              log( e.message, 'ERROR' )
              log( e.backtrace, 'ERROR' )
            else
              tm.rollback 
              log( "Rolling back.", 'ERROR' )
              log( e.message, 'ERROR' )
              log( e.backtrace, 'ERROR' )
            end
            raise e
          end
        else
          TorqueBox.transaction do 
            yield self 
          end
        end
      end

      def add_listener( listener )
        cache.add_listener( listener )
      end

      def stop
        cache.stop
      end

      def self.log( message, status = 'INFO' )
        $stdout.puts( "#{status}: #{message}" )
      end

      def log( message, status = 'INFO' )
        TorqueBox::Infinispan::Cache.log( message, status )
      end

      private

      def encode(data)
        @codec.encode(data)
      end

      def decode(data)
        @codec.decode(data)
      end
      
      def options 
        @options ||= {}
      end

      def cache
        if INFINISPAN_AVAILABLE 
          @cache ||= manager.running?( name ) ? reconfigure : configure
        else
          @cache ||= nothing
        end
      end

      def service
        @service ||= TorqueBox::ServiceRegistry[CacheService::CACHE]
      end

      def manager
        @manager ||= service.cache_container
      end

      def reconfigure(mode=clustering_mode)
        existing_cache  = manager.get_cache(name)
        base_config = existing_cache.cache_configuration
        new_config = configuration(mode)
        unless same_config?(base_config, new_config)
          log("Reconfiguring Infinispan cache #{name}")
          existing_cache.stop
          manager.define_configuration(name, new_config )
          existing_cache.start
        end
        return existing_cache
      end

      def configure(mode=clustering_mode)
        log( "Configuring Infinispan cache #{name}" )
        manager.define_configuration(name, configuration(mode) )
        manager.get_cache(name)
      end

      def configuration(mode=clustering_mode)
        config = ConfigurationBuilder.new.read( manager.default_cache_configuration )
        config.clustering.cacheMode( mode )
        config.transaction.transactionMode( transaction_mode )
        if transactional?
          config.transaction.transactionManagerLookup( transaction_manager_lookup )
          config.transaction.lockingMode( locking_mode )
        end
        if persisted?
          store = config.loaders.add_file_cache_store
          store.purgeOnStartup( false )
          store.location(options[:persist]) if File.exist?( options[:persist].to_s )
        end
        if ( options[:max_entries] ) 
          config.eviction.max_entries( options[:max_entries] )
          if ( options[:eviction] )
            config.eviction.strategy( eviction_strategy )
          end
        end
        config.build
      end

      def same_config?(c1, c2)
        c1.clustering.cacheMode == c2.clustering.cacheMode &&
          (c1.loaders == c2.loaders ||
           (c1.loaders.cacheLoaders.size == c2.loaders.cacheLoaders.size &&
            c1.loaders.cacheLoaders.first.location == c2.loaders.cacheLoaders.first.location)) &&
          c1.transaction.transactionMode == c2.transaction.transactionMode &&
          c1.transaction.lockingMode == c2.transaction.lockingMode &&
          c1.eviction.max_entries == c2.eviction.max_entries &&
          c1.eviction.strategy == c2.eviction.strategy
      end
      
      def transaction_manager_lookup
        @tm ||= if TorqueBox.fetch('transaction-manager')
                  ContainerTransactionManagerLookup.new 
                else
                  org.infinispan.transaction.lookup.GenericTransactionManagerLookup.new
                end
      end

      def nothing
        result = Object.new
        def result.method_missing(*args); end
        log( "Nothing: Can't get or create an Infinispan cache. No caching will occur", 'ERROR' ) if defined?(TORQUEBOX_APP_NAME)
        result
      end

      def __put(key, value, expires, operation)
        # encode
        args = [ operation, encode(key), encode(value) ]
        if expires > 0
          # Set the Infinispan expire a few minutes into the future to support
          # :race_condition_ttl on read
          #expires_in = expires + 300 # 300 seconds == 5 minutes
          expires_in = expires
          args << expires_in << SECONDS
          args << expires << SECONDS
        end
        decode(cache.send(*args))
      end

    end

  end
end


