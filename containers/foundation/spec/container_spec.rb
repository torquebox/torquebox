
require 'torquebox/container/foundation'

describe TorqueBox::Container::Foundation do

  it "should be instantiable" do
    container = TorqueBox::Container::Foundation.new
  end

  it "should be startable" do
    container = TorqueBox::Container::Foundation.new
    container.start
  end

  describe "managing fundamental deployments" do

    before(:each) do
      @container = TorqueBox::Container::Foundation.new
    end

    it "should deploy and undeploy fundamental deployments" do
      @container.fundamental_deployment_paths << File.join( File.dirname(__FILE__), "fund-1-jboss-beans.xml" )
      @container.start()
      deployer = @container['RubyRuntimeFactoryDeployer']
      puts "deployer=#{deployer}"
      @container.stop()
    end
  end

end

class MockDeployer < Java::org.jboss.deployers.spi.deployer.helpers::AbstractDeployer

  def self.simple_name
    "MockDeployer"
  end
end
