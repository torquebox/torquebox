require 'torqbox'
require 'rack/handler'

module Rack
  module Handler
    module TorqBox

      def self.run(app, options={})
        server = ::TorqBox::Server.new({ :host => options[:Host],
                                         :port => options[:Port] })
        yield server if block_given?

        server.start(:rack_app => app)
        thread = Thread.current
        Signal.trap("INT") do
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
