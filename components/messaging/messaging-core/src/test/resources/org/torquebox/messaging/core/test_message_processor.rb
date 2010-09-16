require 'torquebox/messaging/message_processor'

class TestMessageProcessor < TorqueBox::Messaging::MessageProcessor
  
  CONFIG_ONE = { :prop1=>"cheese", :prop2=>42 } 
  
  attr_accessor :opts
  attr_accessor :messages
  
  def initialize()
    @opts = {}
    @messages = []
  end
  
  def configure(opts)
    @opts = opts    
  end
  
  def on_message(body)
    puts "TestMessageProcessor#on_message(#{self.message})"
    @messages << self.message
  end
  
end
