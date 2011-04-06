
require 'jruby'
require 'torquebox/container/foundation'

describe TorqueBox::Container::Foundation do

  it "should be instantiable" do
    container = TorqueBox::Container::Foundation.new
  end

  it "should be startable and stoppable" do
    container = TorqueBox::Container::Foundation.new
    container.start
    container.stop
  end

  describe "foundational deployment" do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @container.start
    end
    after(:each) do
      @container.stop
    end

    it "should deploy a RuntimePoolDeployer" do
      @container['RuntimePoolDeployer'].should_not be_nil
    end

    it "should expose the current Ruby runtime as an MCBean" do
      ruby = @container['Ruby']
      ruby.should_not be_nil
      ruby.should be( JRuby.runtime )
    end

    it "should expose a ruby runtime factory as an MCBean" do
      factory = @container['RubyRuntimeFactory']
      factory.should_not be_nil
      ruby = factory.createInstance( __FILE__ )
      ruby.should be( JRuby.runtime )
    end
    
    it "should be able to deploy a non-existent deployment" do
      deployment = @container.deploy(object_id.to_s)
      unit = @container.deployment_unit( deployment.name )
      unit.should_not be_nil
    end
  end

end
