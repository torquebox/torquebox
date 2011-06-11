
class EchoProcessor < TorqueBox::WebSockets::Processor

  def initialize()
  end

  def start()
    puts "starting"
  end

  def connected()
    puts "connected to #{channel}"
    nil
  end

  def disconnected()
    puts "disconnected from #{channel}"
  end

  def stop() 
    puts "stopping"
  end

  def on_message(msg)
    puts "received #{msg}"
    send "ECHO:#{msg}"
  end

  

end
