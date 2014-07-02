# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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

  it "should coerce :singleton to an actual boolean" do
    val = @scheduler.coerce_schedule_options(singleton: nil)[:singleton]
    val.should == false
    val = @scheduler.coerce_schedule_options(singleton: false)[:singleton]
    val.should == false
    val = @scheduler.coerce_schedule_options(singleton: :foo)[:singleton]
    val.should == true
    val = @scheduler.coerce_schedule_options(singleton: true)[:singleton]
    val.should == true
  end
end
