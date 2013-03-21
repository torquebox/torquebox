require 'torquebox-messaging'

class TestQueueConsumer < TorqueBox::Messaging::MessageProcessor

  def on_message(body)
    puts "on_message: #{body}"
    TorqueBox.fetch('/queues/results').publish( "#{self.class.name}=#{body}" )
  end

end
