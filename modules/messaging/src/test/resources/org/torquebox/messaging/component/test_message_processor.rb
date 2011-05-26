require 'torquebox-messaging'

class TestMessageProcessor < TorqueBox::Messaging::MessageProcessor

  CONFIG_ONE = { :prop1=>"cheese", :prop2=>42 } 

  attr_accessor :opts
  attr_accessor :messages

  def initialize(opts = {})
    @opts = opts
    @messages = []
  end

  def on_message(body)
    @messages << self.message
  end

end
