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
      options = {}
      extension = TorqueBox::CLI.extensions[argv.first]
      parser = OptionParser.new
      if extension
        command = argv.shift
        parser.banner = "Usage: torquebox #{command} #{extension.usage_parameters}"
        parser.separator ""
        parser.separator "#{command} options:"
        extension.setup_parser(parser, options)
      else
        parser.banner = "Usage: torquebox [command] [options]"
        parser.separator ""
        parser.separator "Commands:"
        TorqueBox::CLI.extensions.keys.each do |command|
          description = TorqueBox::CLI.extension_descriptions[command]
          parser.separator "    #{command}: #{description}"
        end
        parser.separator "Installing additional torquebox gems may provide additional commands."
        parser.separator "'torquebox [command] -h' for additional help on each command"
      end

      parser.separator ""
      parser.separator "Common options:"
      parser.on '-q', '--quiet', 'Only write errors to the output' do
        options[:log_level] = 'ERROR'
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
        extension.run(argv, options)
      else
        puts parser
        exit 1
      end
    end
  end
end


# Load all known CLI extensions
# We only rescue LoadError in case the extension was found
# but couldn't be loaded for some other reason
begin
  require 'torquebox/web/cli'
rescue LoadError
  # ignore
end
