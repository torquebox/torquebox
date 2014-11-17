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

require 'torquebox/codecs'
require 'torquebox/caching/cache'

module TorqueBox
  module Caching
    class << self
      include OptionUtils
      extend OptionUtils
      java_import org.projectodd.wunderboss::WunderBoss
      java_import org.projectodd.wunderboss::Options
      java_import org.projectodd.wunderboss.caching::Caching
      VALID_OPTIONS = optset(Caching::CreateOption, :encoding)

      # Returns an
      # {https://docs.jboss.org/infinispan/6.0/apidocs/org/infinispan/Cache.html
      # org.infinispan.Cache}, an extension of
      # `java.util.concurrent.ConcurrentMap`. A name is the only
      # required argument. If a cache by that name already exists, it
      # will be returned, and any options passed to this function will
      # be ignored. To force reconfiguration of an existing cache,
      # call {stop} before calling this function.
      # 
      # *Durability:* Entries can persist to disk via the :persist
      # option. If set to `true`, cache entries will persist in the
      # current directory. Override this by setting `:persist` to a
      # string naming the desired directory.
      #
      # *Eviction:* Turned off by default, `:max-entries` may be set to
      # mitigate the risk of memory exhaustion. When `:persist` is
      # enabled, evicted entries are written to disk, so that the
      # entries in memory are a subset of those in the file store,
      # transparently reloaded upon request. The eviction policy may
      # be one of `:none`, `:lru`, `:lirs`, or `:unordered`
      #
      # *Expiration:* Both time-to-live and max idle limits are
      # supported. Units are milliseconds.
      #
      # *Replication:* The replication mode defaults to `:dist-sync` when
      # clustered. When not clustered, the value of `:mode` is ignored,
      # and the cache will be `:local`. Asynchronous replication may
      # yield a slight performance increase at the risk of potential
      # cache inconsistency.
      #
      # *Transactions:* Caches can participate in transactions when a
      # TransactionManager is available. The locking scheme may be
      # either `:optimisitic` or `:pessimistic`
      #
      # *Advanced configuration:* Infinispan has many buttons,
      # switches, dials, knobs and levers. Call the {builder} function
      # to create your own Configuration instance and pass it in via
      # the `:configuration` option.
      #
      # @param name [String] The name of the cache
      # @param options [Hash] Options for cache creation.
      # @option options :persist [String, true, false] (nil) if non-nil,
      #   data persists across server restarts in a file store; a
      #   string value names the directory
      # @option options :max-entries [Number] (-1) the max number of
      #   entries allowed in the cache
      # @option options :eviction [Symbol] (:none) how entries are
      #   evicted when :max-entries is exceeded
      # @option options :ttl [Number] (-1) the max time the entry will
      #   live before expiry
      # @option options :idle [Number] (-1) the time after which an
      #   entry will expire if not accessed
      # @option options :mode [Symbol] (:dist-sync or :local)
      #   replication mode, one of :local, :repl-sync, :repl-async,
      #   :invalidation-sync, :invalidation-async, :dist-sync,
      #   :dist-async
      # @option options :transactional [true, false] (false) whether
      #   the cache is transactional
      # @option options :locking [Symbol] (:optimistic) transactional
      #   locking scheme
      # @option options :configuration [Configuration] a
      #   {https://docs.jboss.org/infinispan/6.0/apidocs/org/infinispan/configuration/cache/Configuration.html Configuration} instance"
      # @return [Cache] The cache reference
      def cache(name, options = {})
        validate_options(options, VALID_OPTIONS)
        cache = component.find_or_create(name, extract_options(options, Caching::CreateOption))
        codec = Codecs[options.fetch(:encoding, :marshal_smart)]
        Cache.new(component.withCodec(cache, codec), options)
      end

      # Stop cache by name
      # 
      # @param name [String] the name of the cache to stop
      # @return [true, false] true if successfully stopped
      def stop(name)
        component.stop(name)
      end

      # Determine whether cache is currently running
      # 
      # @param name [String] the name of the cache
      # @return [true, false] true if running
      def exists?(name)
        !!component.find(name)
      end

      # For advanced use, call this function to obtain a "fluent"
      # {https://docs.jboss.org/infinispan/6.0/apidocs/org/infinispan/configuration/cache/ConfigurationBuilder.html ConfigurationBuilder}.
      # Set the desired options, and invoke its build method, the
      # result from which can be passed via the :configuration option
      # of the {cache} function.
      #
      # Note that builder takes the same options as {cache}
      def builder(options = {})
        config = org.projectodd.wunderboss.caching::Config
        config.builder(Options.new(extract_options(options, Caching::CreateOption)))
      end

      private

      def component
        @component ||= WunderBoss.find_or_create_component(Caching.java_class)
      end
    end
  end
end
