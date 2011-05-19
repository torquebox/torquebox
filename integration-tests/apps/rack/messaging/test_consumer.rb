require 'torquebox-messaging'

class TestConsumer < TorqueBox::Messaging::MessageProcessor
  include TorqueBox::Injectors
  
  def on_message(body)
    puts "on_message: #{body}"
    inject('queue/results').publish( "result=#{body}" )
  end
  
end
