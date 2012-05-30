require 'torquebox/messaging/message'

include TorqueBox::Messaging

class DummyMessage < Message
  ENCODING = :dummy
end

Message.register_encoding( DummyMessage )

def mock_message
  msg = mock( 'JMSMessage' )
  msg.stub( :get_string_property ).and_return( 'dummy' )
  msg
end

describe TorqueBox::Messaging::Message do

  describe "message property population" do

    it "should do nothing if no properties are submitted" do
      jms_message = mock_message
      jms_message.should_not_receive(:setStringProperty)

      Message.new(jms_message).populate_message_properties nil
    end

    it "should register properties with String keys" do
      properties = {'abc' => 'def'}

      jms_message = mock_message
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register properties with Symbol keys" do
      properties = {:abc => :def}

      jms_message = mock_message
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register true as a boolean" do
      properties = {:abc => true}

      jms_message = mock_message
      jms_message.should_receive(:set_boolean_property).with('abc', true)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register false as a boolean" do
      properties = {:abc => false}

      jms_message = mock_message
      jms_message.should_receive(:set_boolean_property).with('abc', false)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register 5 as an long" do
      properties = {:abc => 5}

      jms_message = mock_message
      jms_message.should_receive(:set_long_property).with('abc', 5)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register 5.5 as a double" do
      properties = {:abc => 5.5}

      jms_message = mock_message
      jms_message.should_receive(:set_double_property).with('abc', 5.5)

      Message.new(jms_message).populate_message_properties properties
    end
  end

  describe 'encodings' do
    before(:each) do
      @class = mock("MessageSubClass", :new => nil)
      @session = mock("Session", :is_a? => true)
    end

    it 'should use the default encoding if none provided' do
      Message.should_receive(:class_for_encoding).with(Message::DEFAULT_ENCODE_ENCODING).and_return(@class)
      Message.new(@session)
    end

    it 'should use the given encoding if provided' do
      Message.should_receive(:class_for_encoding).with(:biscuit).and_return(@class)
      Message.new(@session, nil, :biscuit)
    end

    it 'should use the encoding from the env if set and no encoding is passed to the constructor' do
      ENV['DEFAULT_MESSAGE_ENCODING'] = 'ham'
      Message.should_receive(:class_for_encoding).with(:ham).and_return(@class)
      Message.new(@session)
      ENV['DEFAULT_MESSAGE_ENCODING'] = nil
    end

    it 'should ignore the encoding from the env if set and an encoding is passed to the constructor' do
      ENV['DEFAULT_MESSAGE_ENCODING'] = 'ham'
      Message.should_receive(:class_for_encoding).with(:biscuit).and_return(@class)
      Message.new(@session, nil, :biscuit)
      ENV['DEFAULT_MESSAGE_ENCODING'] = nil
    end
  end

  describe 'delegating to jms_message' do
    before(:each) do
      @jms_msg = mock_message
      @message = Message.new(@jms_msg)
    end

    it "should pass any missing calls through" do
      @jms_msg.should_receive(:ham).with(:biscuit)
      @message.ham(:biscuit)
    end

    it "should properly report if the jms_message responds to the method" do
      @jms_msg.should_receive(:ham).never
      @message.respond_to?(:ham).should be_true
    end

    it "should properly report if the jms_message does not respond to the method" do
      @message.respond_to?(:ham).should_not be_true
    end
  end
  
end
