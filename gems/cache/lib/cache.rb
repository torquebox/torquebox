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
require 'torquebox/injectors'
require 'torquebox/transactions'

module TorqueBox
  module Infinispan

    class ContainerTransactionManagerLookup 
      include TorqueBox::Injectors
      include org.infinispan.transaction.lookup.TransactionManagerLookup
      
      def getTransactionManager
        inject('transaction-manager')
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

    class Sequence
      include java.io.Serializable

      class Codec
        def self.encode(sequence)
          sequence.value.to_s
        end

        def self.decode(sequence_bytes)
          sequence_bytes && Sequence.new( sequence_bytes.to_s.to_i )
        end
      end

      def initialize(amount = 1) 
        @data = amount
      end

      def value
        @data ? @data.to_i : @data
      end

      def next(amount = 1)
        Sequence.new( @data.to_i + amount )
      end

      def ==(other)
        self.value == other.value
      end

      def to_s
        "Sequence: #{self.value}"
      end
    end

    class Cache

      SECONDS = java.util.concurrent.TimeUnit::SECONDS

      def initialize(opts = {})
        @options                      = opts
        @in_container                 = false
        cache
      end

      def name
        options[:name] || TORQUEBOX_APP_NAME
      end

      def search_manager
        @search_manager ||= org.infinispan.query.Search.getSearchManager(@cache)
      end

      def in_container?
        @in_container
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

      def all
        cache.key_set.collect{|k| get(k)}
      end

      def contains_key?( key )
        cache.contains_key( key )
      end

      # Get an entry from the cache 
      def get(key)
        cache.get(key)
      end

      # Write an entry to the cache 
      def put(key, value, expires = 0)
        __put(key, value, expires, :put_async)
      end

      def put_if_absent(key, value, expires = 0)
        __put(key, value, expires, :put_if_absent_async)
      end

      def replace(key, original_value, new_value, codec=NoOpCodec)
        # First, grab the raw value from the cache, which is a byte[]

        current = get( key )
        decoded = codec.decode( current )

        # great!  we've got a byte[] now.  Let's apply == to it, like Jim says will work always

        if ( decoded == original_value )
           # how does this work?
           cache.replace( key, current, codec.encode( new_value ) )
        end
      end

      # Delete an entry from the cache 
      def remove(key)
        cache.removeAsync( key ) && true
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

      include TorqueBox::Injectors
      def transaction(&block)
        if inject('transaction-manager').nil?
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
          end
        else
          TorqueBox.transaction do 
            yield self 
          end
        end
      end

      def stop
        cache.stop
      end

      def self.log( message, status = 'INFO' )
        $stderr.puts( "#{status}: #{message}" )
      end

      def log( message, status = 'INFO' )
        TorqueBox::Infinispan::Cache.log( message, status )
      end

      private

      def options 
        @options ||= {}
      end

      def cache
        @cache ||= clustered || local || nothing
      end

      def manager
        begin
          @manager ||= TorqueBox::ServiceRegistry[org.jboss.msc.service.ServiceName::JBOSS.append( "infinispan", "web" )]
        rescue Exception => e
          log( "Caught exception while looking up Infinispan service.", 'ERROR' )
          log( e.message, 'ERROR' )
        end
        @manager
      end
                       
      def reconfigure(mode=clustering_mode)
        cache = manager.get_cache(name)
        config = cache.configuration
        unless config.cache_mode == mode
          log( "Reconfiguring Infinispan cache #{name} from #{config.cache_mode} to #{mode}" )
          cache.stop
          config.cache_mode = mode
          manager.define_configuration(name, config)
          cache.start
        end
        return cache
      end

      def configure(mode=clustering_mode)
        log( "Configuring Infinispan cache #{name} as #{mode}" )
        config = manager.default_configuration.clone
        config.transaction.recovery.transactionManagerLookup( transaction_manager_lookup )
        config.cache_mode = mode
        config.class_loader = java.lang::Thread.current_thread.context_class_loader
        manager.define_configuration(name, config)
        manager.get_cache(name)
      end

      def transaction_manager_lookup
        @tm ||= if inject('transaction-manager')
                  ContainerTransactionManagerLookup.new 
                else
                  org.infinispan.transaction.lookup.GenericTransactionManagerLookup.new
                end
      end

      def clustered
        (manager.running?(name) ? reconfigure : configure) if manager
      rescue
        log( "Can't get clustered cache; falling back to local: #{$!}", 'ERROR' )
      end

      def local
        log( "Configuring Infinispan local cache #{name}" )
        bare_config              = org.infinispan.config.Configuration.new
        bare_config.class_loader = java.lang::Thread.current_thread.context_class_loader

        config  = bare_config.fluent
        config.transaction.recovery.transactionManagerLookup( transaction_manager_lookup )
        
        if options[:persist]
          log( "Configuring #{name} local cache for file-based persistence" )
          store = org.infinispan.loaders.file.FileCacheStoreConfig.new
          store.purge_on_startup( false )
          store.location(options[:persist]) if File.exist?( options[:persist].to_s ) 
          config.loaders.add_cache_loader( store )
        end

        if options[:index]
          log( "Configuring #{name} local cache for local-only, in-memory indexing" )
          config.indexing.index_local_only(true).add_property('indexing', 'in memory')
        end

        manager = org.infinispan.manager.DefaultCacheManager.new(config.build)
        manager.get_cache()
      rescue Exception => e
        log( "Unable to obtain local cache: #{$!}", 'ERROR' )
        log( e.backtrace, 'ERROR' )
      end
      
      def nothing
        result = Object.new
        def result.method_missing(*args); end
        log( "Can't get or create an Infinispan cache. No caching will occur", 'ERROR' )
        result
      end

      def __put(key, value, expires, operation)
        args = [ operation, key, value ]
        if expires > 0
          # Set the Infinispan expire a few minutes into the future to support
          # :race_condition_ttl on read
          expires_in = expires + 5.minutes
          args << expires_in << SECONDS
        end
        #$stderr.puts "cache=#{cache.inspect}"
        #$stderr.puts "*args=#{args.inspect}"
        cache.send( *args ) && true
      end
    end

  end
end


