# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
require 'torquebox-naming'

describe "naming" do
  context "remote" do
    it "should connect to local" do
      TorqueBox::Naming.remote_context do |context|
        context.to_a.should_not be_empty
      end
    end

    it "should connect with properties" do
      TorqueBox::Naming.remote_context(:host => 'localhost', :port => 4447) do |context|
        context.to_a.should_not be_empty
      end
    end

    it "should get context as return value" do
      context = TorqueBox::Naming.remote_context
      context.to_a.should_not be_empty

      context.close
    end

    it "should lookup via hash" do
      TorqueBox::Naming.remote_context do |context|
        context["jms"].should_not be_nil
      end
    end
  end
end
