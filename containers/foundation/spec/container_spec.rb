
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
  end

end
