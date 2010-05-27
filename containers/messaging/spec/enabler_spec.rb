
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
        puts "FUCK"
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
        puts "A1: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
        @container.start
        puts "A2: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
      rescue => e
        puts "FUCK"
        puts "A3: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
        puts e
        puts e.backtrace
        raise e
      end
      puts "A4: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
      @deployments = []
    end

    after(:each) do
      puts "AFTER EACH"
      puts "A5: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
      @deployments.reverse.each do |deployment|
        puts "undeploy #{deployment}"
        @container.undeploy( deployment )
      end
      puts "A6: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
      @container.stop
    end

    it "should be able to deploy a queues.yml" do
      puts "BEFORE queues.yml"
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      puts "AFTER queues.yml"
      puts "B1: #{Java::java.lang::System.getProperty( 'java.rmi.server.codebase' )}"
      @container.process_deployments(true)
      puts "DONE!"
    end

    it "should be able to deploy a messaging.rb" do
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
      @container.process_deployments(true)
      @deployments << @container.deploy( File.join( File.dirname(__FILE__), 'messaging.rb' ) )
      @container.process_deployments(true)
    end

  end

end
