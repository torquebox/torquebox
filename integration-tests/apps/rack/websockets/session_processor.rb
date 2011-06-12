
class SessionProcessor < TorqueBox::WebSockets::Processor

  def connected()
    puts "Client connected to channel #{channel}"
    food = session[:food]
    puts "Inbound food: #{food}"
    session[:food] = 'beef'
    puts "Reset to beef #{session[:food]}"
    puts "Sending"
    send( food )
    puts "Closing"
    close()
    puts "Closed"
  end

end
