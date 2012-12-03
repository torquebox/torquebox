# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
require 'torquebox-capistrano-support'

describe Capistrano::TorqueBox, "loaded into a configuration" do

  before do
    @configuration = Capistrano::Configuration.new
    Capistrano::TorqueBox.load_into(@configuration)
  end

  context "Ruby interpreter version" do
    it "should not define app_ruby_version by default" do
      @configuration.exists?( :app_ruby_version ).should be_false
    end

    it "should allow setting neither app_ruby_version nor jruby_opts" do
      Capistrano::TorqueBox.load_into(@configuration)
      @configuration.exists?( :app_ruby_version ).should be_false
      @configuration.exists?( :jruby_opts ).should be_false
      expect { @configuration.fetch( :jruby_bin ) }.not_to raise_error
    end

    it "should allow default 1.9 override for app_ruby_version" do
      @configuration.set( :app_ruby_version, 1.9 )
      Capistrano::TorqueBox.load_into(@configuration)
      @configuration.fetch( :app_ruby_version ).should == 1.9
    end

    it "should allow default 1.8 override for app_ruby_version" do
      @configuration.set( :app_ruby_version, 1.8 )
      Capistrano::TorqueBox.load_into(@configuration)
      @configuration.fetch( :app_ruby_version ).should == 1.8
    end

    it "should add app_ruby_version if set to jruby opts if unset" do
      @configuration.set( :app_ruby_version, 1.9 )
      Capistrano::TorqueBox.load_into(@configuration)
      @configuration.fetch( :jruby_opts ).should == "--1.9"
    end
  end

  it "should not modify jruby_opts if already set" do
    @configuration.set( :app_ruby_version, 1.9 )
    @configuration.set( :jruby_opts, "-X+C" )
    Capistrano::TorqueBox.load_into(@configuration)
    @configuration.fetch( :jruby_opts ).should == "-X+C"
  end

  it "should create a deployment descriptor" do
    @configuration.should respond_to(:create_deployment_descriptor)
  end

  it "should set RAILS_ENV based on :rails_env" do
    @configuration.set( :rails_env, 'development' )
    @configuration.create_deployment_descriptor('/path/to/app')['environment']['RAILS_ENV'].should == 'development'
  end

  context "Virtual host configuration" do
    it "should set the web listening host" do
      @configuration.set(:app_host, 'taquito.com')
      @configuration.create_deployment_descriptor('/path/to/app')['web']['host'].should eq('taquito.com')
    end

    it "should not care if there is no app host" do
      @configuration.set(:app_context, "/")
      @configuration.create_deployment_descriptor('/path/to/app/')['web'].should_not have_key('host')
    end
  end

  context "Stomp configuration" do
    it "should set the stomp hostname" do
      @configuration.set(:stomp_host, "awesome.com")
      @configuration.create_deployment_descriptor('/path/to/app')['stomp']['host'].should eq("awesome.com")
    end
  end

end
