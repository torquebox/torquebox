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

    it "should exist after writing" do
      @store.write("key", 42).should be_true
      @store.exist?("key").should be_true
    end

    it "should be gone after deleting" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.delete("key").should be_true
      @store.read("key").should be_nil
    end

    it "should be expirable" do
      @store.write("key", 42, :expires_in => 1.second).should be_true
      @store.read("key").should == 42
      sleep(1.1)
      @store.read("key").should be_nil
    end

    it "should overwrite an existing key" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.write("key", 44).should be_true
      @store.read("key").should == 44
    end

    it "should optionally not overwrite an existing key" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.write("key", 44, :unless_exist => true).should be_true
      @store.read("key").should == 42
    end

  end

end
