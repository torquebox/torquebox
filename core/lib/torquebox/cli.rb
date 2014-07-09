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

    class << self
      def extensions
        @extensions ||= {}
      end
      def extension_descriptions
        @extension_descriptions ||= {}
      end
      def register_extension(command, extension, description)
        extensions[command] = extension
        extension_descriptions[command] = description
      end
    end

    def initialize(argv)
      require_torquebox_gems
      options = {}
      extension = TorqueBox::CLI.extensions[argv.first]
      parser = OptionParser.new
      if extension
        command = argv.shift
        parser.banner = "Usage: torquebox #{command} #{extension.usage_parameters}"
        parser.separator TorqueBox::CLI.extension_descriptions[command]
        parser.separator ""
        parser.separator "#{command} options:"
        extension.setup_parser(parser, options)
      else
        parser.banner = "Usage: torquebox [command] [options]"
        parser.separator ""
        parser.separator "Commands:"
        TorqueBox::CLI.extensions.keys.each do |command|
          description = TorqueBox::CLI.extension_descriptions[command]
          command = "#{command}:".ljust(8)
          parser.separator "    #{command} #{description}"
        end
        parser.separator ""
        parser.separator "Installing additional torquebox gems may provide additional commands."
        parser.separator "'torquebox [command] -h' for additional help on each command"
      end

      parser.separator ""
      parser.separator "Common options:"
      parser.on '-q', '--quiet', 'Log only errors' do
        options[:verbosity] = :quiet
      end
      parser.on '-v', '--verbose', 'Log more - use twice for even more' do
        if options[:verbosity] == :verbose
          options[:verbosity] = :really_verbose
        else
          options[:verbosity] = :verbose
        end
      end
      parser.on_tail('-h', '--help', 'Show this message') do
        puts parser
        exit 1
      end
      parser.on_tail('--version', 'Show version') do
        puts "TorqueBox #{TorqueBox::VERSION}"
        exit 1
      end
      parser.parse!(argv)

      if extension
        log_level = case options.delete(:verbosity)
                    when :quiet then 'ERROR'
                    when :verbose then 'DEBUG'
                    when :really_verbose then 'TRACE'
                    else 'INFO'
                    end
        TorqueBox::Logger.log_level = log_level
        extension.run(argv, options)
      else
        puts parser
        exit 1
      end
    end

    def require_torquebox_gems
      # Ensure all other known TorqueBox gems are loaded so we can see their
      # CLI extensions and jars
      %w(torquebox-web torquebox-messaging torquebox-scheduling torquebox-caching).each do |gem|
        begin
          require gem
        rescue LoadError
        end
      end
    end

  end
end
