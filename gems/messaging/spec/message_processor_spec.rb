require 'torquebox/messaging/message_processor'

class MyTestMessage
  include javax.jms::TextMessage
  attr_accessor :text
  def initialize 
    @properties = {}
  end
  def set_string_property k, v
    @properties[k] = v
  end
  def get_string_property k
    @properties[k]
  end
end

class MyTestProcessor < TorqueBox::Messaging::MessageProcessor
  attr_accessor :body
  def on_message(body)
    self.body = body
  end
end

describe TorqueBox::Messaging::MessageProcessor do
  
  before :each do
    @processor = MyTestProcessor.new
    @jms_message = MyTestMessage.new
  end

  it "should process text messages" do
    @message = TorqueBox::Messaging::Message.new(@jms_message, "foo")
    @processor.process! @message
    @processor.body.should eql("foo")
  end

  it "should process non-text messages" do
    payload = {:foo => "foo", :sym => :sym, "bar" => :bar}
    @message = TorqueBox::Messaging::Message.new(@jms_message, payload)
    @processor.process! @message
    @processor.body.should eql(payload)
  end

end
