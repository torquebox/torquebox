require 'torquebox/messaging/message'

include TorqueBox::Messaging

class DummyMessage < Message
end

Message.register_encoding( :dummy, DummyMessage )

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
    
  end

end
