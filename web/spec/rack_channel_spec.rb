# Copyright 2008-2014 Red Hat, Inc, and individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

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
