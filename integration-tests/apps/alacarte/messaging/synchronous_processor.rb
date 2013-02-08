require 'torquebox-messaging'

class SynchronousProcessor < TorqueBox::Messaging::MessageProcessor
  def on_message(body)
    "Got #{body} but I want bacon!"
  end
end
