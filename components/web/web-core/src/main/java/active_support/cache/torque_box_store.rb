
require 'active_support/cache'
require 'org/torquebox/interp/core/kernel'

module ActiveSupport
  module Cache
    class TorqueBoxStore < Store

      def initialize(options = {})
        super(options)
        cache
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
        old_entry = read_entry( key, options )
        value = old_entry.value.to_i + amount
        new_entry = Entry.new( value, options )
        if cache.replace( key, old_entry, new_entry )
          return value
        else
          raise "Concurrent modification, old value was #{old_entry.value}"
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
        value = cache.get(key)
        value && Marshal.load(String.from_java_bytes(value))
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      def write_entry(key, entry, options = {})
        args = [ :put_async, key, Marshal.dump(entry).to_java_bytes ]
        args[0] = :put_if_absent_async if options[:unless_exist]
        args << options[:expires_in].to_i << java.util.concurrent.TimeUnit::SECONDS if options[:expires_in]
        cache.send( *args ) && true
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, options) # :nodoc:
        cache.removeAsync( key ) && true
      end

      private

      def cache
        @cache ||= clustered || local || nothing
      end

      def clustered
        registry = TorqueBox::Kernel.lookup("CacheContainerRegistry")
        container = registry.cache_container( 'web' )
        result = container.get_cache(TORQUEBOX_APP_NAME)
        logger.info "Using clustered cache: #{result}" if logger
        result
      rescue
        logger.warn("Unable to obtain clustered cache") if logger
        nil
      end

      def local
        container = org.infinispan.manager.DefaultCacheManager.new()
        result = container.get_cache()
        logger.info "Using local cache: #{result}" if logger
        result
      rescue
        logger.warn("Unable to obtain local cache: #{$!}") if logger
        nil
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

