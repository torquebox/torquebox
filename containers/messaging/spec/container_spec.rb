
require 'torquebox/container/messaging'

describe TorqueBox::Container::Messaging do

=begin
  it "should be instantiable" do
    container = TorqueBox::Container::Messaging.new
  end

  it "should be startable" do
    container = TorqueBox::Container::Messaging.new
    container.start
    puts "started container #{container}"
    sleep(2)
    puts "stopping container #{container}"
    container.stop
  end


  it "should set up an RMI class provider" do
    container = TorqueBox::Container::Messaging.new
    container.start
    provider = container['RMIClassProvider']
    provider.should_not be_nil
    puts provider.inspect
    container.stop
  end

  it "should be able to deploy a queues.yml" do
    container = TorqueBox::Container::Messaging.new
    container.start
    queues_yml = container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
    container.process_deployments( true )
    container.undeploy( queues_yml )
    container.stop
  end

  it "should deploy a JMSServerManager" do 
    container = TorqueBox::Container::Messaging.new
    container.start
    jms_server_manager = container['JMSServerManager'] 
    jms_server_manager.should_not be_nil
    container.stop
  end
=end

  it "should be able to deploy a messaging.rb" do
    container = TorqueBox::Container::Messaging.new
    container.start
    puts "started container #{container}"
    #queues_yml   = container.deploy( File.join( File.dirname(__FILE__), 'queues.yml' ) )
    messaging_rb = container.deploy( File.join( File.dirname(__FILE__), 'messaging.rb' ) )
    container.process_deployments( true )
    puts "stopping container #{container}"
    container.undeploy( messaging_rb )
    #container.undeploy( queues_yml )
    container.stop
  end
=begin
=end
end
