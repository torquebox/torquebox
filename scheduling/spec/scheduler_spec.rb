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
