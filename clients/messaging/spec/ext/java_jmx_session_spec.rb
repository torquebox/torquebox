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
      jms_message.should_receive(:setStringProperty).with('abc', 'def')

      @session.populate_message_properties jms_message, properties
    end

    it "should register properties with Symbol keys" do
      properties = {:abc => :def}

      jms_message = mock('JMSMessage')
      jms_message.should_receive(:setStringProperty).with('abc', 'def')

      @session.populate_message_properties jms_message, properties
    end
  end
end
