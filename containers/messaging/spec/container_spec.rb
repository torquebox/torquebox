
require 'torquebox/container/messaging'

describe TorqueBox::Container::Messaging do

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

end
