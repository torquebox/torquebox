require 'org.torquebox.messaging-client'

class UpperCaser < TorqueBox::Messaging::MessageProcessor
  def initialize
    @queue = TorqueBox::Messaging::Queue.new '/queues/responses'
  end
  def on_message(word) 
    @queue.publish word.upcase
  end
end
