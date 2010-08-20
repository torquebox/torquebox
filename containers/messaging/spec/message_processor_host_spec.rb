
require 'torquebox/messaging/message_processor_host'

describe TorqueBox::Messaging::MessageProcessorHost do

  describe "basics" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Messaging::MessageProcessorHost ) 
      begin
        @container.start
      rescue => e
        puts e
        puts e.backtrace
        raise e
      end
    end

    after(:each) do
      @container.stop
    end

    it "should have a MessageProcessorDeployer" do
      @container['MessageProcessorDeployer'].should_not be_nil
    end

  end

end
