require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/message'
require 'torquebox/messaging/marshal_base64_message'

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

class MyTestSession
  include javax.jms::Session

  def create_text_message
    MyTestMessage.new
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
    @jms_session = MyTestSession.new
  end

  it "should process text messages" do
    @message = TorqueBox::Messaging::Message.new(@jms_session, "foo", :marshal_base64)
    @processor.process! @message
    @processor.body.should eql("foo")
  end

  it "should process non-text messages" do
    payload = {:foo => "foo", :sym => :sym, "bar" => :bar}
    @message = TorqueBox::Messaging::Message.new(@jms_session, payload, :marshal_base64)
    @processor.process! @message
    @processor.body.should eql(payload)
  end

  describe "#middleware" do
    it "should return the default middleware" do
      @processor.middleware.inspect.should == "[TorqueBox::Messaging::ProcessorMiddleware::WithTransaction]"
    end
  end
  

end
