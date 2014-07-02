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
require 'stringio'

java_import org.projectodd.wunderboss.rack.RackChannel

describe 'RackChannel' do

  before(:each) do
    RackChannel.create_rack_channel_class(JRuby.runtime)
    @channel = WunderBoss::RackChannel.new
  end

  it "doesn't eat newlines when calling gets" do
    @channel.io = StringIO.new("foo\nbar")
    expect(@channel.gets).to eq("foo\n")
    expect(@channel.gets).to eq("bar")
  end

  it "doesn't read entire input if asked to read more than 4096" do
    @channel.io = StringIO.new("foo\nbar" * 1000)
    expect(@channel.read(4097).length).to eq(4097)
  end
end
