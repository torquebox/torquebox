
class TimeProcessor < TorqueBox::WebSockets::Processor

  def connected()
    puts "Client connected to channel #{channel}"
    send( Time.now.to_s ) do
      puts "Done sent!"
    end
    close()
  end

end
