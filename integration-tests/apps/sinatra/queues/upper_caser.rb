require 'torquebox-messaging'

class UpperCaser < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def initialize
    #@queue = TorqueBox::Messaging::Queue.new '/queues/responses'
    @queue = inject( '/queues/responses' )
  end
  def on_message(word) 
    puts "on_message! #{word}"
    @queue.publish word.upcase
  end
end
