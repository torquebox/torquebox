require 'torquebox/messaging/ext/javax_jms_session'

describe javax.jms::Session do

  before(:each) do
    @session = javax.jms::Session.new
  end

  describe ".populate_message_properties" do
    it "should do nothing if no properties are submitted" do
      jms_message = mock('JMSMessage')
      jms_message.should_not_receive(:setStringProperty)

      @session.populate_message_properties jms_message, nil
    end

    it "should register properties with String keys" do
      properties = {'abc' => 'def'}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      @session.populate_message_properties jms_message, properties
    end

    it "should register properties with Symbol keys" do
      properties = {:abc => :def}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_string_property).with('abc', 'def')

      @session.populate_message_properties jms_message, properties
    end

    it "should register true as a boolean" do
      properties = {:abc => true}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_boolean_property).with('abc', true)

      @session.populate_message_properties jms_message, properties
    end

    it "should register false as a boolean" do
      properties = {:abc => false}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_boolean_property).with('abc', false)

      @session.populate_message_properties jms_message, properties
    end

    it "should register 5 as an long" do
      properties = {:abc => 5}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_long_property).with('abc', 5)

      @session.populate_message_properties jms_message, properties
    end

    it "should register 5.5 as a double" do
      properties = {:abc => 5.5}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:set_double_property).with('abc', 5.5)

      @session.populate_message_properties jms_message, properties
    end
  end
end
