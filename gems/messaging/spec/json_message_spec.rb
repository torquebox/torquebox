require 'torquebox/messaging/message'
require 'torquebox/messaging/json_message'

include TorqueBox::Messaging

class MockMessage
  include javax.jms::Message

  attr_accessor :text
  def get_string_property(_)
    "json"
  end
end

def define_JSON
  klass = Class.new {
    def self.fast_generate(_)
    end
  }
  Object.const_set(:JSON, klass)
end


describe TorqueBox::Messaging::JSONMessage do
  before(:each) do
    @message = Message.new( MockMessage.new )
  end

  after(:each) do
    Object.send(:remove_const, :JSON) if defined?(JSON)
  end

  context "requiring json" do
    it "should raise if json isn't available" do
      @message.should_receive(:require).with('json').and_raise(LoadError.new)
      lambda { @message.encode( 'abc' ) }.should raise_error( RuntimeError )
    end

    it "should not raise if json is available" do
      @message.should_receive(:require).with('json').and_return { define_JSON }
      lambda { @message.encode( 'abc' ) }.should_not raise_error
    end

    it "should only require json once" do
      @message.should_receive(:require).once.with('json').and_return { define_JSON }
      @message.encode( 'abc' )
      @message.encode( 'abc' )
    end
  end
end
