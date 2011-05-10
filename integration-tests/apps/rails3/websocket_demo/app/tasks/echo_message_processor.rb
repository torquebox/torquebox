
class EchoMessageProcessor < TorqueBox::Messaging::WebSocketsProcessor
  
  attr :out_queue
  
  def on_message(body)
    puts "on_message :: received message #{body}."
    send "[ECHO] #{body}."
  end
  
  def on_error(error)
    puts "Encountered error #{error}."
  end
  
end
