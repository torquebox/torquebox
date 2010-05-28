
require 'torquebox/container/foundation'
require 'torquebox/container/messaging_enabler'

describe TorqueBox::Container::MessagingEnabler do

  describe "basics" do

    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Container::MessagingEnabler ) do
      end
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

    it "should have an RMIClassProvider" do
      @container['RMIClassProvider'].should_not be_nil
    end
    
  end

  describe "deployments" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.enable( TorqueBox::Container::MessagingEnabler ) do
      end
      begin
        @container.start
      rescue => e
        puts e
        puts e.backtrace
        raise e
      end
      @deployments = []
    end

    after(:each) do
      @deployments.reverse.each do |deployment|
        @container.undeploy( deployment )
      end
      @container.stop
    end

    it "should be able to deploy a queues.yml" do
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      @container.process_deployments(true)
    end

    it "should be able to deploy a messaging.rb" do
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      @container.process_deployments(true)
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'messaging.rb' ) )
      @container.process_deployments(true)
    end

  end

end
