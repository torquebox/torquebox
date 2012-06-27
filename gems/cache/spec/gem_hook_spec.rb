#
# Copyright 2012 Red Hat, Inc.
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
#

require File.dirname(__FILE__) + '/spec_helper'
require 'gem_hook'

describe 'torquebox-cache gem_hook.rb with ' do
  it "should not load torque_box_store.rb if ActiveSupport is unavailable" do
    lambda { ActiveSupport::Cache::TorqueBoxStore.should raise_error(NameError) }
  end

  it "should load torque_box_store.rb if ActiveSupport is available" do
    module ActiveSupport ; end
    load 'gem_hook.rb'
    lambda { ActiveSupport::Cache::TorqueBoxStore.should_not raise_error(NameError) }
  end
end

