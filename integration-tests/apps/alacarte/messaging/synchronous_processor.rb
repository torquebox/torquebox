require 'torquebox-messaging'

class SynchronousProcessor < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    puts "Received message: #{body}"

    "Got #{body} but I want bacon!"
  end
end
