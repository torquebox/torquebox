require 'torquebox-web'

# Serve the Rack app in the current directory
server = TorqueBox::Web.run(:port => 8080, :auto_start => false)
# Add a SockJS server endpoint that echoes back every message
# sent by a client
server.sockjs(:path => '/echo').on_connection do |conn|
  conn.on_data do |message|
    conn.write(message)
  end
end

server.start
