require 'torquebox-messaging'

class TestTopicConsumer < TorqueBox::Messaging::MessageProcessor

  def on_message(body)
    puts "on_message: #{body}"
    TorqueBox.fetch('/queues/results').publish( "#{self.class.name}=#{body}" )
  end

end
