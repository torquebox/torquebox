require "tubesock"

class Server
  def call(env)
    if env["HTTP_UPGRADE"] == "websocket"
      tubesock = Tubesock.hijack(env)
      tubesock.onmessage do |message|
        tubesock.send_data(message)
      end
      tubesock.listen
      [-1, {}, []]
    else
      [404, {'Content-Type' => 'text/plain'}, ['Not Found']]
    end
  end
end

run Server.new
