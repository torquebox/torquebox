require 'torquebox/messaging/message'

include TorqueBox::Messaging

describe TorqueBox::Messaging::Message do

  describe "message property population" do

    it "should do nothing if no properties are submitted" do
      jms_message = mock('JMSMessage')
      jms_message.should_not_receive(:setStringProperty)

      Message.new(jms_message).populate_message_properties nil
    end

    it "should register properties with String keys" do
      properties = {'abc' => 'def'}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register properties with Symbol keys" do
      properties = {:abc => :def}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register true as a boolean" do
      properties = {:abc => true}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_boolean_property).with('abc', true)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register false as a boolean" do
      properties = {:abc => false}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_boolean_property).with('abc', false)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register 5 as an long" do
      properties = {:abc => 5}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_long_property).with('abc', 5)

      Message.new(jms_message).populate_message_properties properties
    end

    it "should register 5.5 as a double" do
      properties = {:abc => 5.5}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_double_property).with('abc', 5.5)

      Message.new(jms_message).populate_message_properties properties
    end
  end
  
  describe "null handling" do
    it "should decode nil as nil" do
      Message.decode(nil).should be_nil
      jms_message = mock('JMSMessage')
      jms_message.should_receive(:text)
      Message.decode(jms_message).should be_nil
    end

    it "should encode nil as nil" do
      Message.encode(nil).should be_nil
    end
  end
end
