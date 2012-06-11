
require 'torquebox/service_registry'

describe TorqueBox::ServiceRegistry do

  it "should return nil lookup if registry unavailable" do
    TorqueBox::ServiceRegistry.lookup('foo').should be_nil
  end

  it "should return value from registry" do
    @registry["foo"] = "bar"
    TorqueBox::ServiceRegistry.service_registry = @service_registry
    TorqueBox::ServiceRegistry.lookup("foo").should eql("bar")
  end

  it "should yield value from from block when registry contains key" do
    @registry["foo"] = "bar"
    TorqueBox::ServiceRegistry.service_registry = @service_registry
    TorqueBox::ServiceRegistry.lookup("foo") {|x| "baz" }.should eql("baz")
  end

  it "should defer block execution if registry unavailable" do
    @registry["foo"] = "bar"
    TorqueBox::ServiceRegistry.lookup("foo") { |foo| @foo = foo }.should be_nil
    TorqueBox::ServiceRegistry.blocks.should_not be_empty
    @foo = nil
    TorqueBox::ServiceRegistry.service_registry = @service_registry
    TorqueBox::ServiceRegistry.blocks.should be_empty
    @foo.should eql("bar")
  end

  it "should make the registry available" do
    TorqueBox::ServiceRegistry.service_registry = @service_registry
    TorqueBox::ServiceRegistry.registry.should == @service_registry
  end

  # Mock a JBoss ServiceRegistry backed by a simple member Hash,
  # @registry, that holds the fixtures for the tests.
  before(:each) do
    TorqueBox::ServiceRegistry.stub!(:service_name_for).and_return { |name| name }
    TorqueBox::ServiceRegistry.service_registry = nil
    @registry = {}
    @service_registry = mock("service_registry")
    @service_registry.stub!(:getService).and_return { |name|
      service = mock("service")
      service.stub!(:getValue).and_return(@registry[name])
      service
    }
  end
  
end

