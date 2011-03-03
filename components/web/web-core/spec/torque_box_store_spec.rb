require 'active_support/cache/torque_box_store'
require 'logger'

describe ActiveSupport::Cache::TorqueBoxStore do

  before(:each) do
    @store = ActiveSupport::Cache::TorqueBoxStore.new()
    @store.logger = Logger.new(STDOUT)
  end

  describe "basics" do

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

    it "should overwrite an existing key" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.write("key", 44).should be_true
      @store.read("key").should == 44
    end

  end

  describe "options" do

    it "should be expirable" do
      @store.write("key", 42, :expires_in => 1.second).should be_true
      @store.read("key").should == 42
      sleep(1.1)
      @store.read("key").should be_nil
    end

    it "should optionally not overwrite an existing key" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.write("key", 44, :unless_exist => true).should be_true
      @store.read("key").should == 42
    end

    it "should merge initialized options" do
      @store = ActiveSupport::Cache::TorqueBoxStore.new(:expires_in => 1.second)
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      sleep(1.1)
      @store.read("key").should be_nil
    end

  end

  describe "fetching" do

    it "should fetch existing keys" do
      @store.write("today", "Monday")
      @store.fetch("today").should == "Monday"
    end

    it "should fetch block values for missing keys" do
      @store.fetch("city").should be_nil
      @store.fetch("city") {
        "Duckburgh"
      }.should == "Duckburgh"
      @store.fetch("city").should == "Duckburgh"
    end

    it "should fetch block values when forced" do
      @store.write("today", "Monday")
      @store.fetch("today").should == "Monday"
      @store.fetch("today", :force => true) { "Tuesday" }.should == "Tuesday"
    end

  end

  describe "multiples" do

    before(:each) do
      @store.write("john", "guitar")
      @store.write("paul", "bass")
      @store.write("george", "lead")
      @store.write("ringo", "drums")
    end

    it "should delete by regexp" do
      @store.delete_matched /g/
      @store.read("george").should be_nil
      @store.read("ringo").should be_nil
      @store.read("john").should == "guitar"
      @store.read("paul").should == "bass"
    end

    it "should clear all entries" do
      @store.clear
      @store.read("george").should be_nil
      @store.read("ringo").should be_nil
      @store.read("john").should be_nil
      @store.read("paul").should be_nil
    end

    it "should cleanup expired entries" do
      @store.write("jimi", "guitar", :expires_in => 1.second)
      @store.exist?("jimi").should be_true
      sleep(1.1)
      @store.cleanup
      @store.exist?("jimi").should be_false
      @store.exist?("ringo").should be_true
    end

    it "should read multiple values" do
      @store.read_multi("john", "paul").should == {"john" => "guitar", "paul" => "bass"}
    end

  end

  describe "advanced" do
    
    it "should support incrementation" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.increment("key").should == 43
      @store.read("key").should == 43
    end

    it "should support decrementation" do
      @store.write("key", 42).should be_true
      @store.read("key").should == 42
      @store.decrement("key").should == 41
      @store.read("key").should == 41
    end

  end

end
