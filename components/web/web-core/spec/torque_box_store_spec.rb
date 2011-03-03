require 'active_support/cache/torque_box_store'
require 'logger'

describe ActiveSupport::Cache::TorqueBoxStore do

  before(:each) do
    @cache = ActiveSupport::Cache::TorqueBoxStore.new()
    # @cache.logger = Logger.new(STDOUT)
  end

  describe "basics" do

    it "should write and read a string" do
      @cache.write("key", "value").should be_true
      @cache.read("key").should == "value"
    end

    it "should write and read a number" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
    end

    it "should exist after writing" do
      @cache.write("key", 42).should be_true
      @cache.exist?("key").should be_true
    end

    it "should be gone after deleting" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      @cache.delete("key").should be_true
      @cache.read("key").should be_nil
    end

    it "should overwrite an existing key" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      @cache.write("key", 44).should be_true
      @cache.read("key").should == 44
    end

  end

  describe "options" do

    it "should be expirable" do
      @cache.write("key", 42, :expires_in => 1.second).should be_true
      @cache.read("key").should == 42
      sleep(1.1)
      @cache.read("key").should be_nil
    end

    it "should optionally not overwrite an existing key" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      @cache.write("key", 44, :unless_exist => true).should be_true
      @cache.read("key").should == 42
    end

    it "should merge initialized options" do
      @cache = ActiveSupport::Cache::TorqueBoxStore.new(:expires_in => 1.second)
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      sleep(1.1)
      @cache.read("key").should be_nil
    end

  end

  describe "fetching" do

    it "should fetch existing keys" do
      @cache.write("today", "Monday")
      @cache.fetch("today").should == "Monday"
    end

    it "should fetch block values for missing keys" do
      @cache.fetch("city").should be_nil
      @cache.fetch("city") {
        "Duckburgh"
      }.should == "Duckburgh"
      @cache.fetch("city").should == "Duckburgh"
    end

    it "should fetch block values when forced" do
      @cache.write("today", "Monday")
      @cache.fetch("today").should == "Monday"
      @cache.fetch("today", :force => true) { "Tuesday" }.should == "Tuesday"
    end

  end

  describe "multiples" do

    before(:each) do
      @cache.write("john", "guitar")
      @cache.write("paul", "bass")
      @cache.write("george", "lead")
      @cache.write("ringo", "drums")
    end

    it "should delete by regexp" do
      @cache.delete_matched /g/
      @cache.read("george").should be_nil
      @cache.read("ringo").should be_nil
      @cache.read("john").should == "guitar"
      @cache.read("paul").should == "bass"
    end

    it "should clear all entries" do
      @cache.clear
      @cache.read("george").should be_nil
      @cache.read("ringo").should be_nil
      @cache.read("john").should be_nil
      @cache.read("paul").should be_nil
    end

    it "should cleanup expired entries" do
      @cache.write("jimi", "guitar", :expires_in => 1.second)
      @cache.exist?("jimi").should be_true
      sleep(1.1)
      @cache.cleanup
      @cache.exist?("jimi").should be_false
      @cache.exist?("ringo").should be_true
    end

    it "should read multiple values" do
      @cache.read_multi("john", "paul").should == {"john" => "guitar", "paul" => "bass"}
    end

  end

  describe "advanced" do
    
    it "should support incrementation" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      @cache.increment("key").should == 43
      @cache.read("key").should == 43
    end

    it "should support decrementation" do
      @cache.write("key", 42).should be_true
      @cache.read("key").should == 42
      @cache.decrement("key").should == 41
      @cache.read("key").should == 41
    end

  end

end
