require 'active_support/cache/torque_box_store'
require 'logger'

java_import org.infinispan.configuration.cache::CacheMode
include ActiveSupport::Cache

TORQUEBOX_APP_NAME = 'active-support-unit-test'

describe ActiveSupport::Cache::TorqueBoxStore do

  before(:each) do
    manager = org.infinispan.manager.DefaultCacheManager.new 
    service = org.projectodd.polyglot.cache.as.CacheService.new
    service.stub!(:cache_container).and_return( manager )
    TorqueBox::ServiceRegistry.stub!(:[]).with(org.projectodd.polyglot.cache.as.CacheService::CACHE).and_return( service )
    TorqueBox::ServiceRegistry.service_registry = nil
    @cache = ActiveSupport::Cache::TorqueBoxStore.new()
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

    it "should support :race_condition_ttl" do
      database = mock('database')
      fetch_options = { :expires_in => 0.1.seconds, :race_condition_ttl => 30.seconds }
      # First fetch looks up from database and populates
      database.should_receive(:town).and_return("Pantsville")
      @cache.fetch("town", fetch_options) {
        database.town
      }.should == "Pantsville"
      # Sleep until the entry is expired
      sleep(0.2)
      # Create a set of CountDownLatches to test :race_condition_ttl
      # without relying on sleep calls
      read_latch = java.util.concurrent.CountDownLatch.new(1)
      write_latch = java.util.concurrent.CountDownLatch.new(1)
      # Read the cache from two threads but only one should hit our database
      database.should_receive(:town).once.and_return {
        # Trigger the read latch so the other thread can read the cached value
        read_latch.count_down
        write_latch.await(15, java.util.concurrent.TimeUnit::SECONDS)
        "NoPantsville"
      }
      other_thread = Thread.new {
        read_latch.await(15, java.util.concurrent.TimeUnit::SECONDS)
        @cache.fetch("town", fetch_options) {
          database.town
        }.should == "Pantsville"
        # Trigger the write latch to update the cached value
        write_latch.count_down
      }
      @cache.fetch("town", fetch_options) {
        database.town
      }.should == "NoPantsville"
      other_thread.join
      @cache.read("town").should == "NoPantsville"
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

    it "should default to local mode" do
      @cache.clustering_mode.should == CacheMode::LOCAL
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

  describe "options when not clustered" do
    it "should default to :local mode" do
      TorqueBoxStore.new.clustering_mode.to_s.should == "LOCAL"
    end
    
    it "should not fail if set to a clustered mode" do
      TorqueBoxStore.new( :mode => :repl ).clustering_mode.to_s.should == "LOCAL"
    end
  end

end
