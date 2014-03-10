require 'torqbox'
require 'rack/handler'

module Rack
  module Handler
    module TorqBox

      def self.run(app, options={})
        server = ::TorqBox::Server.new({ :host => options[:Host],
                                         :port => options[:Port],
                                         :rack_app => app })
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
        defaults = TorqBox::Server::DEFAULT_OPTIONS
        {
          "Host=HOST" => "Hostname to listen on (default: #{defaults[:host]})",
          "Port=PORT" => "Port to listen on (default: #{defaults[:port]})"
        }
      end
    end

    register :torqbox, TorqBox
  end
end
