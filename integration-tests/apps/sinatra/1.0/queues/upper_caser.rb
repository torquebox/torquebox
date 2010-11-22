require 'org.torquebox.torquebox-messaging-client'

class UpperCaser < TorqueBox::Messaging::MessageProcessor
  def initialize
    puts "JC: UpperCaser initializing..."
    @queue = TorqueBox::Messaging::Queue.new '/queues/responses'
    puts "JC: UpperCaser initialized: #@queue"
  end
  def on_message(word) 
    puts "JC: on_message(#{word})"
    @queue.publish word.upcase
  end
end
