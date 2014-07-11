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
        put_m.call(*[key, value] + expiry(options))
      end
      
      def put_all(map, options={})
        putall_m.call(*[map] + expiry(options))
      end
      
      def put_if_absent(key, value, options={})
        putif_m.call(*[key, value] + expiry(options))
      end
      
      def replace(key, value, options={})
        replace_m.call(*[key, value] + expiry(options))
      end
      
      def compare_and_set(key, old_value, new_value, options={})
        cas_m.call(*[key, old_value, new_value] + expiry(options))
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

      def expiry(options)
        m = defaults(options)
        [m[:ttl], TimeUnit::MILLISECONDS, m[:idle], TimeUnit::MILLISECONDS]
      end

      def replace_m
        @replace_m ||= @cache.java_method(:replace, [java.lang.Object,
                                                     java.lang.Object,
                                                     Java::long,
                                                     java.util.concurrent.TimeUnit,
                                                     Java::long,
                                                     java.util.concurrent.TimeUnit])
      end

      def cas_m
        @cas_m ||= @cache.java_method(:replace, [java.lang.Object,
                                                 java.lang.Object,
                                                 java.lang.Object,
                                                 Java::long,
                                                 java.util.concurrent.TimeUnit,
                                                 Java::long,
                                                 java.util.concurrent.TimeUnit])
      end

      def put_m
        @put_m ||= @cache.java_method(:put, [java.lang.Object,
                                             java.lang.Object,
                                             Java::long,
                                             java.util.concurrent.TimeUnit,
                                             Java::long,
                                             java.util.concurrent.TimeUnit])
      end

      def putall_m
        @putall_m ||= @cache.java_method(:putAll, [java.util.Map,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit,
                                                   Java::long,
                                                   java.util.concurrent.TimeUnit])
      end

      def putif_m
        @putif_m ||= @cache.java_method(:putIfAbsent, [java.lang.Object,
                                                       java.lang.Object,
                                                       Java::long,
                                                       java.util.concurrent.TimeUnit,
                                                       Java::long,
                                                       java.util.concurrent.TimeUnit])
      end

    end
  end
end
