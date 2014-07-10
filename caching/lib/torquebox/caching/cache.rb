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

      def initialize(a_real_cache)
        @cache = a_real_cache
      end

      def replace(key, old=nil, v)
        ttl = idle = -1         # TODO: how to best pass these?
        if old.nil? 
          replace2.call(key, v, ttl, TimeUnit::MILLISECONDS, idle, TimeUnit::MILLISECONDS)
        else
          replace3.call(key, old, v, ttl, TimeUnit::MILLISECONDS, idle, TimeUnit::MILLISECONDS)
        end
      end
      
      def clear
        @cache.clear
        self
      end

      def_delegators :@cache, :size, :put, :put_all, :get, :name,
                              :[], :[]=, :keys, :values, :remove,
                              :put_if_absent, :evict, :contains_key?,
                              :entry_set, :empty?

      def_delegator :@cache, :cache_configuration, :configuration

      attr_accessor :cache

      private

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
    end
  end
end
