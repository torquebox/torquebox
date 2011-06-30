require 'torquebox-messaging'

class MyConsumer < TorqueBox::Messaging::MessageProcessor
  def on_message(msg)
    puts "received: #{msg}"
  end
end
