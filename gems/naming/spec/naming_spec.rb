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

require 'torquebox/naming'

describe TorqueBox::Naming do

  describe "remote context" do
    it "should connect with default values and return context" do
      ctx = mock("InitialContext")

      javax.naming::InitialContext.should_receive(:new).with(
          "java.naming.provider.url" => "remote://localhost:4447",
          "java.naming.factory.initial" => "org.jboss.naming.remote.client.InitialContextFactory"
      ).and_return(ctx)

      TorqueBox::Naming.remote_context.should == ctx
    end

    it "should connect with default values and yield context" do
      ctx = mock("InitialContext")
      ctx.should_receive(:close)

      javax.naming::InitialContext.should_receive(:new).with(
          "java.naming.provider.url" => "remote://localhost:4447",
          "java.naming.factory.initial" => "org.jboss.naming.remote.client.InitialContextFactory"
      ).and_return(ctx)

      TorqueBox::Naming.remote_context do |context|
        ctx.should eql(context)
      end
    end

    it "should allow to specify properties" do
      ctx = mock("InitialContext")

      javax.naming::InitialContext.should_receive(:new).with(
          "java.naming.provider.url" => "remote://remotehost.com:1234",
          "java.naming.factory.initial" => "org.jboss.naming.remote.client.InitialContextFactory"
      ).and_return(ctx)

      TorqueBox::Naming.remote_context(:host => 'remotehost.com', :port => 1234).should == ctx
    end

    it "should allow to set username and password" do
      ctx = mock("InitialContext")

      javax.naming::InitialContext.should_receive(:new).with(
          "java.naming.provider.url" => "remote://remotehost.com:1234",
          "java.naming.factory.initial" => "org.jboss.naming.remote.client.InitialContextFactory",
          "java.naming.security.principal" => "user1",
          "java.naming.security.credentials" => "pass1"
      ).and_return(ctx)

      TorqueBox::Naming.remote_context(:host => 'remotehost.com', :port => 1234, :username => 'user1', :password => 'pass1').should == ctx
    end
  end
end
