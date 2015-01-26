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

require 'torquebox/cli'
require 'torquebox-web'

module TorqueBox
  module Web
    # @api private
    class CLI

      attr_reader :options

      def usage_parameters
        "[options] [rackup_file]"
      end

      def setup_parser(parser, options)
        ENV['RACK_ENV'] = ENV['RAILS_ENV'] = 'development'
        parser.on '-b', '--bind-address IP', 'IP or host to bind to' do |arg|
          options[:host] = arg
        end
        parser.on '--context-path PATH', 'Web context path to use (default: /)' do |arg|
          options[:path] = arg
        end
        parser.on '--dir DIR', 'Change directory before starting' do |arg|
          options[:root] = arg
        end
        parser.on('-e', '--env ENVIRONMENT',
                  'Environment to run under (default: development)') do |arg|
          ENV['RACK_ENV'] = ENV['RAILS_ENV'] = arg
        end
        parser.on '-p', '--port PORT', Integer, 'HTTP port to listen on' do |arg|
          options[:port] = arg
        end
        parser.on '--io-threads N', Integer, 'Number of IO threads (default: # CPUs)' do |arg|
          options[:io_threads] = arg
        end
        parser.on('--worker-threads N', Integer,
                  'Number of HTTP worker threads (default: # CPUs * 8)') do |arg|
          options[:worker_threads] = arg
        end
        parser.on('--[no-]dispatch',
                  'Enable dispatching of requests to worker threads (default: enabled)') do |arg|
          options[:dispatch] = arg
        end
      end

      def run(argv, options)
        unless argv.empty?
          options[:rackup] = argv.shift
        end

        if options[:root]
          org.projectodd.wunderboss.WunderBoss.put_option('root', options[:root])
        end

        # We always want direct control over starting / stopping
        options[:auto_start] = false

        @options = options
        @server = ::TorqueBox::Web.run(options)
        unless ENV['TORQUEBOX_CLI_SPECS']
          @server.run_from_cli
        end
      end
    end
  end
end

TorqueBox::CLI.register_extension('run', TorqueBox::Web::CLI.new,
                                  'Run TorqueBox web server')
