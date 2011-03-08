
require 'torquebox/kernel'

describe TorqueBox::Kernel do

  it "should return nil lookup if registry unavailable" do
    TorqueBox::Kernel.lookup('foo').should be_nil
  end

  it "should return value from registry" do
    @registry["foo"] = "bar"
    TorqueBox::Kernel.kernel = @kernel
    TorqueBox::Kernel.lookup("foo").should eql("bar")
  end

  it "should yield value from from block when registry contains key" do
    @registry["foo"] = "bar"
    TorqueBox::Kernel.kernel = @kernel
    TorqueBox::Kernel.lookup("foo") {|x| "baz" }.should eql("baz")
  end

  it "should defer block execution if registry unavailable" do
    @registry["foo"] = "bar"
    TorqueBox::Kernel.lookup("foo") { |foo| @foo = foo }.should be_nil
    TorqueBox::Kernel.blocks.should_not be_empty
    @foo = nil
    TorqueBox::Kernel.kernel = @kernel
    TorqueBox::Kernel.blocks.should be_empty
    @foo.should eql("bar")
  end

  # Mock a JBoss MicroContainer kernel backed by a simple member Hash,
  # @registry, that holds the fixtures for the tests.
  before(:each) do
    TorqueBox::Kernel.kernel = nil
    @registry = {}
    registry = mock("registry")
    registry.stub!(:findEntry).and_return { |name| 
      entry = mock("entry")
      entry.stub!(:getTarget).and_return(@registry[name])
      entry
    }
    @kernel = mock("kernel")
    @kernel.stub!(:getRegistry).and_return(registry)
  end
  
end
