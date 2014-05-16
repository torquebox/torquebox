require 'spec_helper'


describe "Scheduler" do
  before(:all) do
    @scheduler = TorqueBox::Scheduling::Scheduler
  end

  describe "schedule" do
    it "should work" do
      latch = java.util.concurrent.CountDownLatch.new(0)
      @scheduler.schedule(:latched, {}) { latch.count_down }
      latch.await(1, java.util.concurrent.TimeUnit::SECONDS).should == true
    end

    it "should take a string or symbol for the id" do
      @scheduler.schedule(:foo, {}) {}
      (@scheduler.schedule(:foo, {}) {}).should == true
      (@scheduler.schedule("foo", {}) {}).should == true
    end
  end
end
