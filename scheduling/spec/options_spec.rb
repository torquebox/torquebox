require 'spec_helper'
require 'active_support/time'

class TorqueBox::Scheduling::Scheduler
  public :coerce_schedule_options
end

describe "option coercion" do
  before(:all) do
    @scheduler = TorqueBox::Scheduling::Scheduler.new("default")
  end

  [:at, :until].each do |key|
    it "should coerce a Time #{key} to a java date" do
      now = Time.now
      val = @scheduler.coerce_schedule_options(key => now)[key]
      val.class.should == java.util.Date
      now.to_i.should == val.time / 1000
    end

    it "should coerce a numeric #{key} to a java date" do
      now = Time.now.to_i
      val = @scheduler.coerce_schedule_options(key => now)[key]
      val.class.should == java.util.Date
      now.should == val.time / 1000
    end
  end

  it "should convert activesupport durations to ms" do
    val = @scheduler.coerce_schedule_options(in: 5.seconds)[:in]
    val.should == 5000
  end

  it "should leave numbers alone" do
    val = @scheduler.coerce_schedule_options(in: 5)[:in]
    val.should == 5
  end

  it "should leave a duration converted to millseconds alone" do
    val = @scheduler.coerce_schedule_options(in: 5.seconds.in_milliseconds)[:in]
    val.should == 5000
  end
end
