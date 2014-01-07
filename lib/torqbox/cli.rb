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

module TorqBox
  class CLI
    attr_reader :server

    def initialize(argv)
      @boot_options = {}
      @app_options = {}
      ENV['RACK_ENV'] = ENV['RAILS_ENV'] = 'development'
      OptionParser.new do |opts|
        opts.banner = 'Usage: torqbox [options] [rackup file]'

        opts.on '-b', '--bind-address IP', 'IP or host to bind to' do |arg|
          @boot_options[:host] = arg
        end
        opts.on '--dir DIR', 'Change directory before starting' do |arg|
          @boot_options[:root] = arg
        end
        opts.on '-E', '--env ENVIRONMENT', 'Environment to run under (default: development)' do |arg|
          ENV['RACK_ENV'] = ENV['RAILS_ENV'] = arg
        end
        opts.on '-p', '--port PORT', 'HTTP port to listen on' do |arg|
          @boot_options[:port] = arg
        end
        opts.on '-q', '--quiet', 'Only write errors to the output' do
          @boot_options[:log_level] = 'ERROR'
        end
        opts.on_tail('-h', '--help', 'Show this message') do
          puts opts
          exit 1
        end
        opts.on_tail('--version', 'Show version') do
          puts "TorqBox #{TorqBox::VERSION}"
          exit 1
        end
      end.parse!(argv)

      unless argv.empty?
        @app_options[:rackup] = argv.shift
      end

      @server = ::TorqBox::Server.new(@boot_options)
    end

    def start
      @server.start(@app_options)
    end

    def stop
      @server.stop
    end
  end
end
