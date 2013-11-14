require 'torqbox'
require 'rack/handler'

module Rack
  module Handler
    module TorqBox

      DEFAULT_OPTIONS = {
        :Host => 'localhost',
        :Port => 8080
      }

      def self.run(app, options={})
        options = DEFAULT_OPTIONS.merge(options)

        server = ::TorqBox::Server.new({ :host => options[:Host],
                                         :port => options[:Port] })
        yield server if block_given?

        puts "TorqBox #{::TorqBox::VERSION} starting..."
        server.start(app)
        Signal.trap("INT") do
          puts "\nStopping TorqBox..."
          server.stop
        end
      end

      def self.valid_options
        {
          "Host=HOST" => "Hostname to listen on (default: #{DEFAULT_OPTIONS[:Host]})",
          "Port=PORT" => "Port to listen on (default: #{DEFAULT_OPTIONS[:Port]})"
        }
      end
    end

    register :torqbox, TorqBox
  end
end
