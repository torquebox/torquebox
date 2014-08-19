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

      def cache(name, options = {})
        validate_options(options, VALID_OPTIONS)
        cache = component.find_or_create(name, extract_options(options, Caching::CreateOption))
        codec = Codecs[options.fetch(:encoding, :marshal_smart)]
        Cache.new(component.encodedWith(codec, cache), options)
      end

      def stop(name)
        component.stop(name)
      end

      def exists?(name)
        !!component.find(name)
      end

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
