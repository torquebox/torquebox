require 'active_support/cache/torque_box_store'
require 'logger'

describe ActiveSupport::Cache::TorqueBoxStore do

  describe "basics" do

    before(:each) do
      @store = ActiveSupport::Cache::TorqueBoxStore.new()
      @store.logger = Logger.new(STDOUT)
    end

    it "should write and read a string" do
      @store.write("key", "value").should be_true
      @store.read("key").should == "value"
    end

    it "should write and read a number" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
    end

  end

end
