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

require 'forwardable'

java_import java.util.concurrent::TimeUnit

module TorqueBox
  module Caching
    class Cache
      extend Forwardable

      def initialize(a_real_cache, options={})
        @cache = a_real_cache
        @options = options
      end

      def put(key, value, options={})
        opts = defaults(options)
        putter.call(key, value, opts[:ttl], TimeUnit::MILLISECONDS, opts[:idle], TimeUnit::MILLISECONDS)
      end
      
      def put_all(map, options={})
        opts = defaults(options)
        putaller.call(map, opts[:ttl], TimeUnit::MILLISECONDS, opts[:idle], TimeUnit::MILLISECONDS)
      end
      
      def put_if_absent(key, value, options={})
        opts = defaults(options)
        putiffer.call(key, value, opts[:ttl], TimeUnit::MILLISECONDS, opts[:idle], TimeUnit::MILLISECONDS)
      end
      
      def replace(key, value, options={})
        opts = defaults(options)
        replace2.call(key, value, opts[:ttl], TimeUnit::MILLISECONDS, opts[:idle], TimeUnit::MILLISECONDS)
      end
      
      def compare_and_set(key, old_value, new_value, options={})
        opts = defaults(options)
        replace3.call(key, old_value, new_value, opts[:ttl], TimeUnit::MILLISECONDS, opts[:idle], TimeUnit::MILLISECONDS)
      end
      
      def clear
        @cache.clear
        self
      end

      def_delegators :@cache, :size, :get, :name, :[], :[]=, :keys, :values, :remove,
                              :evict, :contains_key?, :entry_set, :empty?

      def_delegator :@cache, :cache_configuration, :configuration

      attr_accessor :cache

      private

      def defaults(options)
        {ttl: -1, idle: -1}.merge(@options).merge(options)
      end

      def replace2
        @replace2 ||= @cache.java_method(:replace, [java.lang.Object,
                                                    java.lang.Object,
                                                    Java::long,
                                                    java.util.concurrent.TimeUnit,
                                                    Java::long,
                                                    java.util.concurrent.TimeUnit])
      end

      def replace3
        @replace3 ||= @cache.java_method(:replace, [java.lang.Object,
                                                    java.lang.Object,
                                                    java.lang.Object,
                                                    Java::long,
                                                    java.util.concurrent.TimeUnit,
                                                    Java::long,
                                                    java.util.concurrent.TimeUnit])
      end

      def putter
        @putter ||= @cache.java_method(:put, [java.lang.Object,
                                              java.lang.Object,
                                              Java::long,
                                              java.util.concurrent.TimeUnit,
                                              Java::long,
                                              java.util.concurrent.TimeUnit])
      end

      def putaller
        @putaller ||= @cache.java_method(:putAll, [java.util.Map,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit])
      end

      def putiffer
        @putiffer ||= @cache.java_method(:putIfAbsent, [java.lang.Object,
                                                        java.lang.Object,
                                                        Java::long,
                                                        java.util.concurrent.TimeUnit,
                                                        Java::long,
                                                        java.util.concurrent.TimeUnit])
      end

    end
  end
end
