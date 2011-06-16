#
# Copyright 2011 Red Hat, Inc.
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

describe DataMapper::Adapters::InfinispanAdapter do
  before :all do
    @adapter = DataMapper.setup(:default, :adapter   => 'infinispan',
                                          :hostname  => 'localhost',
                                          :port      => 1978)
  end

  it "should behave like an adapter" do
    pending "making the dm built-in tests work" do
      it_should_behave_like 'An Adapter'
    end
  end

end

