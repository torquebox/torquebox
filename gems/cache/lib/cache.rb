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
require 'sequence'

module TorqueBox
  module Infinispan

    # @api private
    class ContainerTransactionManagerLookup
      include TorqueBox::Injectors
      begin
        include org.infinispan.transaction.lookup.TransactionManagerLookup
      rescue NameError
        # Not running inside TorqueBox
      end

      def getTransactionManager
        fetch('transaction-manager')
      end
    end

    class NoOpCodec
      def self.encode(object)
        object
      end

      def self.decode(object)
        object
      end
    end

    class Cache
      include TorqueBox::Injectors

      SECONDS = java.util.concurrent.TimeUnit::SECONDS
      begin
        java_import org.infinispan.configuration.cache::CacheMode
        java_import org.infinispan.configuration.cache::ConfigurationBuilder
        java_import org.infinispan.transaction::TransactionMode
        java_import org.infinispan.transaction::LockingMode
        java_import org.projectodd.polyglot.cache.as::CacheService
        INFINISPAN_AVAILABLE = true
      rescue NameError => e
        INFINISPAN_AVAILABLE = false
        # Not running inside TorqueBox
      end

      def initialize(opts = {})
        return nothing unless INFINISPAN_AVAILABLE
        @options = opts
        options[:transaction_mode] = :transactional unless options.has_key?( :transaction_mode )
        options[:locking_mode] ||= :optimistic if (transactional? && !options.has_key?( :locking_mode ))
        options[:sync] = true if options[:sync].nil?
        cache
      end

      def name
        options[:name] || TORQUEBOX_APP_NAME
      end

      def persisted?
        !options[:persist].nil?
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

      # Clear the entire cache. Be careful with this method since it could
      # affect other processes if shared cache is being used.
      def clear
        cache.clearAsync
      end

      # Return the keys in the cache; potentially very expensive depending on configuration
      def keys
        cache.key_set
      end

      def all
        cache.key_set.collect{|k| get(k)}
      end

      def contains_key?( key )
        cache.contains_key( key.to_s )
      end

      # Get an entry from the cache 
      def get(key)
        cache.get( key.to_s )
      end

      # Write an entry to the cache 
      def put(key, value, expires = 0)
        __put(key, value, expires, :put_async)
      end

      def put_if_absent(key, value, expires = 0)
        __put(key, value, expires, :put_if_absent_async)
      end

      def evict( key )
        cache.evict( key.to_s )
      end

      def replace(key, original_value, new_value, codec=NoOpCodec)
        # First, grab the raw value from the cache, which is a byte[]

        current = get( key )
        decoded = codec.decode( current )

        # great!  we've got a byte[] now.  Let's apply == to it, like Jim says will work always

        if ( decoded == original_value )
           # how does this work?
           cache.replace( key.to_s, current, codec.encode( new_value ) )
        end
      end

      # Delete an entry from the cache 
      def remove(key)
        cache.removeAsync( key.to_s ) && true
      end

      def increment( sequence_name, amount = 1 )
        current_entry = Sequence::Codec.decode( get( sequence_name )  )

        # If we can't find the sequence in the cache, create a new one and return
        put( sequence_name, Sequence::Codec.encode( Sequence.new( amount ) ) ) and return amount if current_entry.nil?

        # Increment the sequence, stash it, and return
        next_entry = current_entry.next( amount )

        # Since replace() doesn't encode, let's encode everything to a byte[] for it, no?
        if replace( sequence_name, current_entry, next_entry, Sequence::Codec  )
          return next_entry.value
        else
          raise "Concurrent modification, old value was #{current_entry.value} new value #{next_entry.value}"
        end
      end

      # Decrement an integer value in the cache; return new value
      def decrement(name, amount = 1)
        increment( name, -amount )
      end

      def transaction(&block)
        if !transactional?
          yield self
        elsif fetch('transaction-manager').nil?
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
        unless base_config.clustering.cache_mode == mode
          log( "Reconfiguring Infinispan cache #{name} from #{base_config.cache_mode} to #{mode}" )
          existing_cache.stop
          configure(mode)
          existing_cache.start
        end
        return existing_cache
      end

      def configure(mode=clustering_mode)
        log( "Configuring Infinispan cache #{name} as #{mode}" )
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
        manager.define_configuration(name, config.build )
        manager.get_cache(name)
      end

      def transaction_manager_lookup
        @tm ||= if fetch('transaction-manager')
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
        args = [ operation, key.to_s, value ]
        if expires > 0
          # Set the Infinispan expire a few minutes into the future to support
          # :race_condition_ttl on read
          #expires_in = expires + 300 # 300 seconds == 5 minutes
          expires_in = expires
          args << expires_in << SECONDS
          args << expires << SECONDS
        end
        cache.send( *args ) && true
      end

    end

  end
end


