require 'torquebox-messaging-processor'

class TestMessageProcessor < TorqueBox::Messaging::Processor
  
  CONFIG_ONE = Marshal.dump( { :prop1=>"cheese", :prop2=>42 } )
  
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