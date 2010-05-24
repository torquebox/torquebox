
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
=end


  it "should be able to deploy a messaging.rb" do
    container = TorqueBox::Container::Messaging.new
    container.start
    puts "started container #{container}"
    container.deploy( File.join( File.dirname(__FILE__), 'messaging.rb' ) )
    container.process_deployments( true )
    puts "stopping container #{container}"
    container.stop
  end

end
