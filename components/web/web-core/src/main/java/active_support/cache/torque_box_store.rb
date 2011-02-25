
require 'org/torquebox/interp/core/kernel'

module ActiveSupport
  module Cache
    class TorqueBoxStore < Store

      def initialize(*args)
        super(*args)
        @cache = clustered || local || nothing
      end

      # Clear the entire cache. Be careful with this method since it could
      # affect other processes if shared cache is being used.
      def clear(options = nil)
        @cache.clearAsync
      end

      # Delete all entries with keys matching the pattern.
      def delete_matched( matcher, options = nil )
        pattern = key_matcher( matcher, options )
        @cache.keySet.each { |key| delete( key, options ) if key =~ pattern }
      end

      # Increment an integer value in the cache.
      #
      # TODO: what are the concurrent expectations for this?
      #
      def increment(name, amount = 1, options = nil)
        raise NotImplementedError.new("#{self.class.name} does not support increment")
      end

      # Increment an integer value in the cache.
      #
      # TODO: what are the concurrent expectations for this?
      #
      def decrement(name, amount = 1, options = nil)
        raise NotImplementedError.new("#{self.class.name} does not support decrement")
      end

      # Cleanup the cache by removing expired entries.
      #
      # TODO: Infinispan equivalent? 
      #
      # All implementations may not support this method.
      def cleanup(options = nil)
        raise NotImplementedError.new("#{self.class.name} does not support cleanup")
      end

      protected

      # Read an entry from the cache implementation. Subclasses must implement this method.
      def read_entry(key, options)
        @cache.get( key )
      end

      # Write an entry to the cache implementation. Subclasses must implement this method.
      #
      # TODO: support :expires_in and :unless_exist
      #
      def write_entry(key, entry, options)
        @cache.putAsync( key, entry )
      end

      # Delete an entry from the cache implementation. Subclasses must implement this method.
      def delete_entry(key, options) # :nodoc:
        @cache.removeAsync( key )
      end

      private

      def clustered
        registry = TorqueBox::Kernel.lookup("CacheContainerRegistry")
        container = registry.cache_container( 'web' )
        result = container.get_cache(TORQUEBOX_APP_NAME)
        puts "Using clustered cache: #{result}"
        result
      rescue
        puts "Unable to obtain clustered cache"
      end

      def local
        container = org.infinispan.manager.DefaultCacheManager.new()
        result = container.get_cache()
        puts "Using local cache: #{result}"
        result
      rescue
        puts "Unable to obtain local cache"
      end
      
      def nothing
        result = Object.new
        def result.method_missing(*args); end
        puts "No caching will occur"
        result
      end

    end
  end
end

