
require 'torquebox/container/foundation'

describe TorqueBox::Container::Foundation do

  it "should be instantiable" do
    container = TorqueBox::Container::Foundation.new
  end

  it "should be startable" do
    container = TorqueBox::Container::Foundation.new
    container.start
  end

  describe "managing deployers" do

    before :each do
      @container = TorqueBox::Container::Foundation.new
      @container.start
    end
  
    after :each do
      @container.stop
      @container = nil
    end
  
    it "should provide access to deployer manipulation" do
      @container.deployers.should_not be_nil
    end
  
    it "should accept a deployer" do
      @container.deployers << MockDeployer.new
    end
  
  end

end

class MockDeployer < Java::org.jboss.deployers.spi.deployer.helpers::AbstractDeployer

  def self.simple_name
    "MockDeployer"
  end
end
