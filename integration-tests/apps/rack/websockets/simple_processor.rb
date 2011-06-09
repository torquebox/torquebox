
class SimpleProcessor

  def initialize()
  end

  def session=(session)
    puts "handed session #{session}"
    @session = session
  end

  def session() 
    @session
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
    "OKAY!"
  end

  

end
