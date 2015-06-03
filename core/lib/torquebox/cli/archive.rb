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

require 'optparse'

module TorqueBox
  class CLI
    class Archive
      def initialize(argv)
        @argv = argv
        @main = ENV['TORQUEBOX_MAIN']
        @rackup = ENV['TORQUEBOX_RACKUP']

        if @main
          @parser = OptionParser.new
          @parser.on('-h', '--help', 'Show this message') do |arg|
            puts @parser
            exit 1
          end
        else
          @cli = TorqueBox::CLI.new(['run'] + @argv)
          @parser = @cli.parser
        end

        @parser.separator ""
        @parser.separator "Archive options:"
        @parser.on('-S SCRIPT',
                   'Run a script from inside the archive (rake tasks, etc)') do |arg|
          # Treat any unprocessed args as belonging to -S so that things like
          # "java -jar foo.jar -S torquebox help" work without quotes
          arg = ([arg] + @argv).join(' ')
          app_jar = java.lang.System.get_property("torquebox.app_jar")
          load_path = $LOAD_PATH.map { |entry| "-I#{entry}" }.join(' ')
          jars = Dir.glob("../jars/*.jar").map { |j| "-r#{j}" }.join(' ')
          # PWD is $tmpdir/app and jruby.jar is $tmpdir/jars/jruby.jar
          Kernel.exec("java -Dtorquebox.app_jar=#{app_jar} \
                      -Djava.io.tmpdir=#{java.lang.System.get_property('java.io.tmpdir')} \
                      -jar ../jars/jruby.jar #{load_path} -r#{app_jar} \
                      #{jars} -rbundler/setup -rtorquebox/cli/archive_cleaner \
                      -S #{arg}")
        end
      end

      def run
        @parser.parse!(@argv)

        if @main
          require @main
        else
          require 'torquebox-web'
          if TorqueBox.in_wildfly?
            TorqueBox::Web.run(:rackup => @rackup)
          else
            @cli.run
          end
        end
      rescue OptionParser::InvalidOption => e
        puts e.message
        puts
        puts @parser
        exit 1
      end
    end
  end
end
