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

require "pathname"

# Set the Thread context classloader since many Java libs we
# bring in use it to locate their classes
java.lang.Thread.current_thread
  .set_context_class_loader(JRuby.runtime.jruby_class_loader)

module TorqueBox

  # @api private
  JAR_MARKER = "torquebox_jar_marker"

  # @api private
  EXPLODED_JAR = !JRuby.classloader_resources(JAR_MARKER).empty?

  class Jars
    class << self
      def register_and_require(jar)
        path = Pathname.new(jar)
        raise 'Jars must be registered using absolute paths' unless path.absolute?
        jar = path.expand_path.to_s
        @jars ||= []
        @jars << jar
        require jar unless EXPLODED_JAR
      end

      def list
        @jars
      end
    end
  end

  class << self
    def in_wildfly?
      org.projectodd.wunderboss.WunderBoss.inWildFly
    end
  end
end

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  TorqueBox::Jars.register_and_require(jar)
end

TorqueBox::Jars.register_and_require("#{File.dirname(__FILE__)}/torquebox-core.jar")
require 'torquebox/cli'
require 'torquebox/cli/archive'
require 'torquebox/cli/jar'
require 'torquebox/cli/war'
require 'torquebox/daemon'
require 'torquebox/logger'
require 'torquebox/option_utils'
require 'torquebox/version'
require 'torquebox/codecs'
