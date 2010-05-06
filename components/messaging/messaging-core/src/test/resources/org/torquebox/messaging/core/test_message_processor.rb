
require 'activesupport'
require 'activemessaging'

class TestMessageProcessor < ActiveMessaging::Processor
  
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
  
  def on_message(message)
    @messages << message
  end
  
end