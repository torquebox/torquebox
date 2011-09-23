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
      extend PathHelper

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should use JBOSS_HOME environment if it is set' do
        TorqueBox::DeployUtils.jboss_home.downcase.should == "#{absolute_prefix}/torquebox/jboss".downcase
      end

      it 'should use TORQUEBOX_HOME environment if JBOSS_HOME is not set' do
        ENV['JBOSS_HOME'] = nil
        TorqueBox::DeployUtils.jboss_home.downcase.should == "#{absolute_prefix}/torquebox/jboss".downcase
      end

      it 'should raise an error if neither JBOSS_HOME or TORQUEBOX_HOME is set' do
        ENV['JBOSS_HOME'] = nil
        ENV['TORQUEBOX_HOME'] = nil
        lambda { TorqueBox::DeployUtils.jboss_home }.should raise_error
      end

    end

    describe 'TorqueBox::DeployUtils.torquebox_home' do
      extend PathHelper

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
      end

      it 'should use TORQUEBOX_HOME environment variable' do
        TorqueBox::DeployUtils.torquebox_home.downcase.should == "#{absolute_prefix}/torquebox".downcase
      end

    end

    describe 'TorqueBox::DeployUtils.jboss_conf' do
      extend PathHelper
      it 'should defafult to "standalone"' do
        TorqueBox::DeployUtils.jboss_conf.should == 'standalone'
      end

      it 'should use ENV["JBOSS_CONF"] if it exists' do
        ENV['JBOSS_CONF'] = 'domain'
        TorqueBox::DeployUtils.jboss_conf.should == ENV['JBOSS_CONF']
      end

      it 'should use ENV["TORQUEBOX_CONF"] if it exists' do
        ENV['TORQUEBOX_CONF'] = 'foobar'
        TorqueBox::DeployUtils.jboss_conf.should == ENV['TORQUEBOX_CONF']
      end

      it 'should prefer ENV["TORQUEBOX_CONF"] if it exists over ENV["JBOSS_CONF"]' do
        ENV['JBOSS_CONF'] = 'domain'
        ENV['TORQUEBOX_CONF'] = 'foobar'
        TorqueBox::DeployUtils.jboss_conf.should == ENV['TORQUEBOX_CONF']
      end
    end

    describe 'TorqueBox::DeployUtils.sys_root' do
      extend PathHelper
      it 'should be /' do
        TorqueBox::DeployUtils.sys_root.should == '/'
      end
    end

    describe 'TorqueBox::DeployUtils.opt_dir' do
      extend PathHelper
      it 'should be /opt' do
        TorqueBox::DeployUtils.opt_dir.should == '/opt'
      end
    end

    describe 'TorqueBox::DeployUtils.opt_torquebox' do
      extend PathHelper
      it 'should be /opt' do
        TorqueBox::DeployUtils.opt_torquebox.should == '/opt/torquebox'
      end
    end

    describe 'TorqueBox::DeployUtils.server_dir' do
      extend PathHelper
      it 'should default to ENV["JBOSS_HOME"]/standalone' do
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = nil
        ENV['TORQUEBOX_CONF'] = nil
        TorqueBox::DeployUtils.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/standalone".downcase
      end

      it 'should use ENV["JBOSS_HOME"]/ENV["JBOSS_CONF"] if JBOSS_CONF is available' do
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = 'foobar'
        ENV['TORQUEBOX_CONF'] = nil
        TorqueBox::DeployUtils.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/foobar".downcase
      end

      it 'should use ENV["JBOSS_HOME"]/ENV["TORQUEBOX_CONF"] if TORQUEBOX_CONF is available' do
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = 'foobar'
        ENV['TORQUEBOX_CONF'] = 'boofar'
        TorqueBox::DeployUtils.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/boofar".downcase
      end
    end

    describe 'TorqueBox::DeployUtils.config_dir' do
      extend PathHelper
      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/opt/torquebox'
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = nil
        ENV['TORQUEBOX_CONF'] = nil
      end
      
      it 'should be ENV["JBOSS_HOME"]/standalone/configuration' do
        TorqueBox::DeployUtils.config_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/configuration".downcase
      end

      it 'should be ENV["JBOSS_HOME"]/standalone/configuration/torquebox/standalone-preview-ha.xml' do
        TorqueBox::DeployUtils.cluster_config_file.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/configuration/torquebox/standalone-preview-ha.xml".downcase
      end
    end


    describe 'TorqueBox::DeployUtils.properties_dir' do
      extend PathHelper
      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/opt/torquebox'
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = nil
        ENV['TORQUEBOX_CONF'] = nil
      end
      
      it 'should be ENV["JBOSS_HOME"]/standalone/configuration' do
        TorqueBox::DeployUtils.properties_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/configuration".downcase
      end

      it 'should be the same as TorqueBox::DeployUtils.config_dir' do
        TorqueBox::DeployUtils.properties_dir.downcase.should == TorqueBox::DeployUtils.config_dir.downcase
      end
    end

    describe 'TorqueBox::DeployUtils.deploy_dir' do
      extend PathHelper
      before( :each ) do
        ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
        ENV['JBOSS_CONF'] = nil
        ENV['TORQUEBOX_CONF'] = nil
      end
      it 'should point to JBOSS_HOME/JBOSS_CONF/deployments' do
        TorqueBox::DeployUtils.deploy_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/deployments".downcase
      end
    end

    describe 'TorqueBox::DeployUtils.modules_dir' do
      extend PathHelper

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should be where I expect it to be' do
        TorqueBox::DeployUtils.modules_dir.downcase.should == "#{absolute_prefix}/torquebox/jboss/modules".downcase
      end

    end

    describe 'TorqueBox::DeployUtils.torquebox_modules_dir' do
      extend PathHelper

      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      end

      it 'should be where I expect it to be' do
        TorqueBox::DeployUtils.torquebox_modules_dir.downcase.should == "#{absolute_prefix}/torquebox/jboss/modules/org/torquebox".downcase
      end

    end

    describe 'TorqueBox::DeployUtils.run_server' do
      before( :each ) do
        ENV['TORQUEBOX_HOME'] = '/torquebox'
        ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
        Dir.stub!(:chdir).and_yield
        Kernel.stub!(:exec)
        TorqueBox::DeployUtils.stub!(:exec_command)
      end

      it 'should pass options to run_command_line' do
        options = { :clustered => true, :max_threads => 25 }
        TorqueBox::DeployUtils.should_receive(:run_command_line).with(options).and_return([])
        TorqueBox::DeployUtils.run_server(options)
      end
    end

    describe 'TorqueBox::DeployUtils.run_command_line' do
      it 'should not add --server-config when not clustered' do
        command, options = TorqueBox::DeployUtils.run_command_line(:clustered => false)
        options.should_not include('--server-config=')
      end

      it 'should add --server-config when clustered' do
        command, options = TorqueBox::DeployUtils.run_command_line(:clustered => true)
        options.should include('--server-config=')
      end

      it 'should not set max threads by default' do
        command, options = TorqueBox::DeployUtils.run_command_line
        options.should_not include('-Dorg.torquebox.web.http.maxThreads=')
      end

      it 'should set max threads when given' do
        command, options = TorqueBox::DeployUtils.run_command_line(:max_threads => 5)
        options.should include('-Dorg.torquebox.web.http.maxThreads=5')
      end

      it 'should not set bind address by default' do
        command, options = TorqueBox::DeployUtils.run_command_line
        options.should_not include('-b ')
      end

      it 'should set bind address when given' do
        command, options = TorqueBox::DeployUtils.run_command_line(:bind_address => '0.0.0.0')
        options.should include('-b 0.0.0.0')
      end
    end

  end
end

