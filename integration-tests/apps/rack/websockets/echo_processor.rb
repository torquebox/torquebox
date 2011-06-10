
class EchoProcessor < TorqueBox::WebSockets::Processor

  def initialize()
  end

  def start()
    puts "starting"
  end

  def connected(channel)
    puts "connected to #{channel}"
    puts "Session is #{session}"
    #puts "  foo is #{session.getAttribute('food')}"
  end

  def disconnected(channel)
    puts "disconnected from #{channel}"
  end

  def stop() 
    puts "stopping"
  end

  def on_message(msg)
    puts "received #{msg}"
    "ECHO:#{msg}"
  end

  

end
