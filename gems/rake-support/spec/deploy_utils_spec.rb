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

require 'spec_helper'

module TorqueBox
  module DeployUtils

    describe 'TorqueBox::DeployUtils.jboss_home' do

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should use JBOSS_HOME environment if it is set' do
        TorqueBox::DeployUtils.jboss_home.should == '/torquebox/jboss'
      end

      it 'should use TORQUEBOX_HOME environment if JBOSS_HOME is not set' do
        ENV['JBOSS_HOME'] = nil
        TorqueBox::DeployUtils.jboss_home.should == '/torquebox/jboss'
      end

      it 'should raise an error if neither JBOSS_HOME or TORQUEBOX_HOME is set' do
        ENV['JBOSS_HOME'] = nil
        ENV['TORQUEBOX_HOME'] = nil
        lambda { TorqueBox::DeployUtils.jboss_home }.should raise_error
      end

    end

    describe 'TorqueBox::DeployUtils.torquebox_home' do

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
      end

      it 'should use TORQUEBOX_HOME environment variable' do
        TorqueBox::DeployUtils.torquebox_home.should == '/torquebox'
      end

    end

    describe 'TorqueBox::DeployUtils.modules_dir' do

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should be where I expect it to be' do
        TorqueBox::DeployUtils.modules_dir.should == '/torquebox/jboss/modules'
      end

    end

    describe 'TorqueBox::DeployUtils.torquebox_modules_dir' do

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should be where I expect it to be' do
        TorqueBox::DeployUtils.torquebox_modules_dir.should == '/torquebox/jboss/modules/org/torquebox'
      end

    end

  end
end

