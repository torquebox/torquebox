require 'torquebox-web'
require 'torquebox/spec_helpers'

# Serve the Rack app in the current directory
server = TorqueBox::Web.run(:port => 8080, :auto_start => false,
                            :root => File.dirname(__FILE__))
# Add a SockJS server endpoint that echoes back every message
# sent by a client
server.sockjs(:path => '/echo').on_connection do |conn|
  conn.on_data do |message|
    conn.write(message)
  end
end

server.start

TorqueBox::SpecHelpers.booted
