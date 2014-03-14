require 'torquebox/cli'
require 'torquebox-web'

module TorqueBox
  module Web
    class CLI

      attr_reader :server

      def usage_parameters
        "[options] [rackup_file]"
      end

      def setup_parser(parser, options)
        ENV['RACK_ENV'] = ENV['RAILS_ENV'] = 'development'
        parser.on '-b', '--bind-address IP', 'IP or host to bind to' do |arg|
          options[:host] = arg
        end
        parser.on '--context-path PATH', 'Web context path to use (default: /)' do |arg|
          options[:context_path] = arg
        end
        parser.on '--dir DIR', 'Change directory before starting' do |arg|
          options[:root] = arg
        end
        parser.on '-E', '--env ENVIRONMENT', 'Environment to run under (default: development)' do |arg|
          ENV['RACK_ENV'] = ENV['RAILS_ENV'] = arg
        end
        parser.on '-p', '--port PORT', 'HTTP port to listen on' do |arg|
          options[:port] = arg
        end
      end

      def run(argv, options)
        unless argv.empty?
          options[:rackup] = argv.shift
        end

        @server = ::TorqueBox::Server.new(options)
        unless ENV['TORQUEBOX_CLI_SPECS']
          @server.start
          thread = Thread.current
          Signal.trap("INT") do
            @server.stop
            thread.wakeup
          end
          Signal.trap("TERM") do
            @server.stop
            thread.wakeup
          end
          sleep
        end
      end
    end
  end
end

TorqueBox::CLI.register_extension('run', TorqueBox::Web::CLI.new,
                                  'Run TorqueBox web server')
