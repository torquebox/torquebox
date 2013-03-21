require 'torquebox-messaging'

class UpperCaser < TorqueBox::Messaging::MessageProcessor

  def initialize
    @queue = TorqueBox.fetch( '/queues/responses' )
  end
  def on_message(word)
    puts "on_message! #{word}"
    @queue.publish word.upcase
  end
end
