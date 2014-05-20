# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

require "pathname"

module TorqueBox

  JAR_MARKER = "torquebox_jar_marker"
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
end

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  TorqueBox::Jars.register_and_require(jar)
end

TorqueBox::Jars.register_and_require("#{File.dirname(__FILE__)}/torquebox-core.jar")
require 'torquebox/cli'
require 'torquebox/cli/jar'
require 'torquebox/logger'
require 'torquebox/option_utils'
require 'torquebox/version'
