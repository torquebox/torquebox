require 'torquebox'
require 'rack/handler'

module Rack
  module Handler
    module TorqueBox

      def self.run(app, options={})
        log_level = 'INFO'
        if options[:Quiet]
          log_level = 'ERROR'
        elsif options[:Verbose]
          log_level = 'DEBUG'
        elsif options[:ReallyVerbose]
          log_level = 'TRACE'
        end
        org.projectodd.wunderboss.WunderBoss.log_level = log_level

        server_options = {
          :host => options[:Host],
          :port => options[:Port],
          :auto_start => false,
          :rack_app => app }
        server = ::TorqueBox::Web::Server.run('default', server_options)
        yield server if block_given?

        server.start
        thread = Thread.current
        Signal.trap("INT") do
          server.stop
          thread.wakeup
        end
        Signal.trap("TERM") do
          server.stop
          thread.wakeup
        end
        sleep
      end

      def self.valid_options
        defaults = ::TorqueBox::Web::Server::DEFAULT_CREATE_OPTIONS
        {
          "Host=HOST" => "Hostname to listen on (default: #{defaults[:host]})",
          "Port=PORT" => "Port to listen on (default: #{defaults[:port]})",
          "Quiet" => "Log only errors",
          "Verbose" => "Log more",
          "ReallyVerbose" => "Log a lot"
        }
      end
    end

    register :torquebox, TorqueBox
  end
end
