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

module TorqueBox
  module Caching
    class Cache
      extend Forwardable

      def initialize(a_real_cache)
        @cache = a_real_cache
      end

      def replace(key, old, v)
        replacer.call(key, old, v)
      end

      def_delegators :@cache, :clear, :size, :put, :get, :name,
                              :[], :[]=, :keys, :values, :remove,
                              :put_if_absent, :evict, :contains_key?,
                              :cache_configuration

      private

      def replacer
        @replacer ||= @cache.java_method(:replace, [java.lang.Object, java.lang.Object, java.lang.Object])
      end
    end
  end
end
