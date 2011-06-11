
class SessionProcessor < TorqueBox::WebSockets::Processor

  def connected()
    puts "Client connected to channel #{channel}"
    food = session.getAttribute( "food" )
    send( food )
    close()
  end

end
