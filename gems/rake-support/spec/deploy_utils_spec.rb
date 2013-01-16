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

describe TorqueBox::DeployUtils do
  before(:each) do
    @util = TorqueBox::DeployUtils
  end

  describe '.jboss_home' do
    extend PathHelper

    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
    end

    it 'should use JBOSS_HOME environment if it is set' do
      @util.jboss_home.downcase.should == "#{absolute_prefix}/torquebox/jboss".downcase
    end

    it 'should use TORQUEBOX_HOME environment if JBOSS_HOME is not set' do
      ENV['JBOSS_HOME'] = nil
      @util.jboss_home.downcase.should == "#{absolute_prefix}/torquebox/jboss".downcase
    end

    it 'should raise an error if neither JBOSS_HOME or TORQUEBOX_HOME is set' do
      ENV['JBOSS_HOME'] = nil
      ENV['TORQUEBOX_HOME'] = nil
      lambda { @util.jboss_home }.should raise_error
    end

  end

  describe '.torquebox_home' do
    extend PathHelper

    it 'should return nil if TORQUEBOX_HOME is not set and torquebox-server is not installed' do
      ENV['TORQUEBOX_HOME'] = nil
      TorqueBox::Server.should_receive( :torquebox_home ).and_return( nil )
      @util.torquebox_home.should == nil
    end

    it 'should use torquebox-server if TORQUEBOX_HOME is not set and torquebox-server is installed' do
      ENV['TORQUEBOX_HOME'] = nil
      TorqueBox::Server.should_receive( :torquebox_home ).and_return( 'torquebox-server-install-path' )
      @util.torquebox_home.should == 'torquebox-server-install-path'
    end

    it 'should use TORQUEBOX_HOME environment variable' do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      @util.torquebox_home.downcase.should == "#{absolute_prefix}/torquebox".downcase
    end

  end

  describe '.jboss_conf' do
    extend PathHelper
    it 'should default to "standalone"' do
      @util.jboss_conf.should == 'standalone'
    end

    it 'should use ENV["JBOSS_CONF"] if it exists' do
      ENV['JBOSS_CONF'] = 'domain'
      @util.jboss_conf.should == ENV['JBOSS_CONF']
    end

    it 'should use ENV["TORQUEBOX_CONF"] if it exists' do
      ENV['TORQUEBOX_CONF'] = 'foobar'
      @util.jboss_conf.should == ENV['TORQUEBOX_CONF']
    end

    it 'should prefer ENV["TORQUEBOX_CONF"] if it exists over ENV["JBOSS_CONF"]' do
      ENV['JBOSS_CONF'] = 'domain'
      ENV['TORQUEBOX_CONF'] = 'foobar'
      @util.jboss_conf.should == ENV['TORQUEBOX_CONF']
    end
  end

  describe '.sys_root' do
    extend PathHelper
    it 'should be /' do
      @util.sys_root.should == '/'
    end
  end

  describe '.opt_dir' do
    extend PathHelper
    it 'should be /opt' do
      @util.opt_dir.should == '/opt'
    end
  end

  describe '.opt_torquebox' do
    extend PathHelper
    it 'should be /opt' do
      @util.opt_torquebox.should == '/opt/torquebox'
    end
  end

  describe '.server_dir' do
    extend PathHelper
    it 'should default to ENV["JBOSS_HOME"]/standalone' do
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = nil
      ENV['TORQUEBOX_CONF'] = nil
      @util.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/standalone".downcase
    end

    it 'should use ENV["JBOSS_HOME"]/ENV["JBOSS_CONF"] if JBOSS_CONF is available' do
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = 'foobar'
      ENV['TORQUEBOX_CONF'] = nil
      @util.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/foobar".downcase
    end

    it 'should use ENV["JBOSS_HOME"]/ENV["TORQUEBOX_CONF"] if TORQUEBOX_CONF is available' do
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = 'foobar'
      ENV['TORQUEBOX_CONF'] = 'boofar'
      @util.server_dir.downcase.should == "#{absolute_prefix}/opt/torquebox/jboss/boofar".downcase
    end
  end

  describe '.config_dir' do
    extend PathHelper
    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/opt/torquebox'
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = nil
      ENV['TORQUEBOX_CONF'] = nil
    end

    it 'should be ENV["JBOSS_HOME"]/standalone/configuration' do
      @util.config_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/configuration".downcase
    end
  end


  describe '.properties_dir' do
    extend PathHelper
    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/opt/torquebox'
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = nil
      ENV['TORQUEBOX_CONF'] = nil
    end

    it 'should be ENV["JBOSS_HOME"]/standalone/configuration' do
      @util.properties_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/configuration".downcase
    end

    it 'should be the same as @util.config_dir' do
      @util.properties_dir.downcase.should == @util.config_dir.downcase
    end
  end

  describe '.deploy_dir' do
    extend PathHelper
    before( :each ) do
      ENV['JBOSS_HOME'] = '/opt/torquebox/jboss'
      ENV['JBOSS_CONF'] = nil
      ENV['TORQUEBOX_CONF'] = nil
    end
    it 'should point to JBOSS_HOME/JBOSS_CONF/deployments' do
      @util.deploy_dir.downcase.should == "#{absolute_prefix}#{ENV['JBOSS_HOME']}/standalone/deployments".downcase
    end
  end

  describe '.modules_dir' do
    extend PathHelper

    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
    end

    it 'should be where I expect it to be' do
      @util.modules_dir.downcase.should == "#{absolute_prefix}/torquebox/jboss/modules".downcase
    end

  end

  describe '.torquebox_modules_dir' do
    extend PathHelper

    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
    end

    it 'should be where I expect it to be' do
      @util.torquebox_modules_dir.downcase.should == "#{absolute_prefix}/torquebox/jboss/modules/org/torquebox".downcase
    end

  end

  describe '.check_server' do
    context "when it can't find the modules" do
      before(:each) do
        File.stub(:exist?).and_return(false)
      end

      it "should raise if it can't find the torquebox modules" do
        lambda { @util.check_server }.should raise_error
      end

      it "should give a helpful error message" do
        begin
          @util.check_server
        rescue Exception => e
          e.message.should =~ /doesn't appear to be a valid TorqueBox install/
        end
      end
    end
  end

  describe '.run_server' do
    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      Dir.stub!(:chdir).and_yield
      Kernel.stub!(:exec)
      @util.stub!(:exec)
      @util.stub!(:exec_command)
    end

    it 'should pass options to run_command_line' do
      options = { :clustered => true, :max_threads => 25 }
      @util.should_receive(:run_command_line).with(options).and_return([])
      @util.run_server(options)
    end

    it 'should check if the current directory is deployed' do
      @util.should_receive(:is_deployed?).and_return( true )
      @util.run_server
    end

    it 'should set java options' do
      @util.should_receive(:set_java_opts).with('java options')
      @util.run_server(:jvm_options => 'java options')
    end

    it 'should pass JRUBY_OPTS properties' do
      ENV['JRUBY_OPTS'] = '--1.9 -Xjit.logging=true -Xthread.pool.enabled=true'
      @util.should_receive(:set_java_opts).with('-Djruby.jit.logging=true -Djruby.thread.pool.enabled=true')
      @util.run_server
    end

    it 'should set java options and pass JRUBY_OPTS properties' do
      ENV['JRUBY_OPTS'] = '-X-C -Xjit.logging=true --ng'
      @util.should_receive(:set_java_opts).with('java options -Djruby.jit.logging=true')
      @util.run_server(:jvm_options => 'java options')
    end

    it 'should strip and pass JRUBY_OPTS jvm options' do
      ENV['JRUBY_OPTS'] = '--1.9 -J-Xmx1024m -J-Xss2048k -Xjit.logging=true'
      @util.should_receive(:set_java_opts).with('some java options -Djruby.jit.logging=true -Xmx1024m -Xss2048k')
      @util.run_server(:jvm_options => 'some java options')
      ENV['JRUBY_OPTS'].should == '--1.9 -Xjit.logging=true'
    end
  end

  describe '.is_deployed?' do
    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      @myapp = @util.deployment_name( 'my-app' )
    end

    it 'should be false if the app is not deployed' do
      @util.is_deployed?( @myapp ).should be_false
    end

    it 'should be true if the app has been deployed' do
      File.stub('exists?').with(File.join(@util.torquebox_home, 'apps')).and_return false
      File.stub('exists?').with(File.join(@util.deploy_dir, @myapp)).and_return true
      @util.is_deployed?( @myapp ).should be_true
    end

    it 'should default to the default deployment_name' do
      @util.should_receive(:deployment_name).and_return @myapp
      @util.is_deployed?
    end

  end

  describe '.run_command_line' do
    it 'should add --server-config when clustered' do
      command, options = @util.run_command_line(:clustered => true)
      options.should include('--server-config=')
    end

    it 'should not set max threads by default' do
      command, options = @util.run_command_line
      options.should_not include('-Dorg.torquebox.web.http.maxThreads=')
    end

    it 'should set max threads when given' do
      command, options = @util.run_command_line(:max_threads => 5)
      options.should include('-Dorg.torquebox.web.http.maxThreads=5')
    end

    it 'should not set bind address by default' do
      command, options = @util.run_command_line
      options.should_not include('-b ')
    end

    it 'should set bind address when given' do
      command, options = @util.run_command_line(:bind_address => '0.0.0.0')
      options.should include('-b 0.0.0.0')
    end

    it 'should not set port offset by default' do
      command, options = @util.run_command_line
      options.should_not include('-Djboss.socket.binding.port-offset')
    end

    it 'should set port offset when given' do
      command, options = @util.run_command_line(:port_offset => '100')
      options.should include('-Djboss.socket.binding.port-offset=100')
    end

    it 'should not set node name by default' do
      command, options = @util.run_command_line
      options.should_not include('-Djboss.node.name')
    end

    it 'should set node name when given' do
      command, options = @util.run_command_line(:node_name => 'mynode')
      options.should include('-Djboss.node.name=mynode')
    end

    it 'should not set data directory by default' do
      command, options = @util.run_command_line
      options.should_not include('-Djboss.server.data.dir')
    end

    it 'should set data directory when given' do
      command, options = @util.run_command_line(:data_directory => '/tmp/mynode')
      options.should include('-Djboss.server.data.dir=/tmp/mynode')
    end

    it 'should allow extra parameters to be passed through to JBoss AS' do
      command, options = @util.run_command_line(:pass_through => '--help')
      options.should include('--help')
    end

    it 'should set the http port' do
      command, options = @util.run_command_line(:port => 9090)
      options.should include('-Dhttp.port=9090')
    end

    it 'should not set HTTP port by default' do
      command, options = @util.run_command_line
      options.should_not include('-Dhttp.port')
    end
  end

  describe '.create_archive' do

    ALL_EXPECTED_FILES = %w{"config.ru"
                            "app/app.rb"
                            "app/puppet-master.rb"
                            "app/app.box"
                            "app/a-non-cached.gem"
                            "vendor/vendor.rb"
                            "puppet/puppet.rb"
                            "simpleapp.box"}
    
    def mock_run_command(expected_includes)
      @util.should_receive(:run_command) do |arg|
        included_files = arg =~ /@(.*)/ ? File.new($1).readlines.map(&:strip) : []
        expected_includes.permutation.to_a.should include(included_files)
      end
    end
    
    it 'should not include excluded dirs and files' do
      mock_run_command(ALL_EXPECTED_FILES - %w{"puppet/puppet.rb" "simpleapp.box"})

      path = @util.create_archive(
          :name => "simpleapp",
          :app_dir => File.join(File.dirname(__FILE__), 'fixtures/simpleapp'),
          :dest_dir => "/tmp",
          :exclude => "puppet,simpleapp.box"
      )
      path.should == "/tmp/simpleapp.knob"
    end

    it 'should exclude based on patterns' do
      mock_run_command(ALL_EXPECTED_FILES - %w{"simpleapp.box"})

      path = @util.create_archive(
          :name => "simpleapp",
          :app_dir => File.join(File.dirname(__FILE__), 'fixtures/simpleapp'),
          :dest_dir => "/tmp",
          :exclude => ".box"
      )
      path.should == "/tmp/simpleapp.knob"
    end

    it 'should include all dirs and files except default' do
      mock_run_command ALL_EXPECTED_FILES
      
      path = @util.create_archive(
          :name => "simpleapp",
          :app_dir => File.join(File.dirname(__FILE__), 'fixtures/simpleapp'),
          :dest_dir => "/tmp"
      )
      path.should == "/tmp/simpleapp.knob"
    end
  end

  describe '.deployment_status' do
    before( :each ) do
      ENV['TORQUEBOX_HOME'] = '/torquebox'
      ENV['JBOSS_HOME'] = ENV['TORQUEBOX_HOME'] + '/jboss'
      @myapp = @util.deployment_name( 'my-app' )
      @appname = @myapp.sub /\-knob.yml/, ''
      File.stub('exists?').with(File.join(@util.torquebox_home, 'apps')).and_return false
      File.stub('exists?').with(File.join(@util.deploy_dir, @myapp)).and_return true
      File.stub('exists?').with(File.join(@util.deploy_dir, "#{@myapp}.dodeploy")).and_return false
      File.stub('exists?').with(File.join(@util.deploy_dir, "#{@myapp}.deployed")).and_return false
      File.stub('exists?').with(File.join(@util.deploy_dir, "#{@myapp}.failed")).and_return false
      Dir.stub('glob').with( "#{@util.deploy_dir}/*-knob.yml" ).and_return [ File.join( @util.deploy_dir, @myapp ) ]
    end

    it 'should return a hash of deployment info keyed by application name' do
      @util.deployment_status[@appname].should_not be_nil
    end

    it 'should provide the deployment descriptor path' do
      @util.deployment_status[@appname][:descriptor].should_not be_nil
    end

    it 'should provide a deployment status if awaiting deployment' do
      dodeploy_file = File.join(@util.deploy_dir, "#{@myapp}.dodeploy")
      File.stub('exists?').with(dodeploy_file).and_return true
      @util.deployment_status[@appname][:status].should == 'awaiting deployment'
    end

    it 'should provide a deployment status if deployed' do
      deployed_file = File.join(@util.deploy_dir, "#{@myapp}.deployed")
      File.stub('exists?').with(deployed_file).and_return true
      @util.deployment_status[@appname][:status].should == 'deployed'
    end

    it 'should provide a deployment status if failed' do
      failed_file   = File.join(@util.deploy_dir, "#{@myapp}.failed")
      File.stub('exists?').with(failed_file).and_return true
      @util.deployment_status[@appname][:status].should == 'deployment failed'
    end
  end

  describe '.rubylib_with_bundler' do
    before(:each) do
      @rubylib = ENV['RUBYLIB']
    end

    after(:each) do
      ENV['RUBYLIB'] = @rubylib
    end

    it 'should equal ENV["RUBYLIB"] if bundler not in load path' do
      ENV['RUBYLIB'] = '/path/to/somewhere'
      @util.rubylib_with_bundler([]).should == '/path/to/somewhere'
    end

    it 'should be empty if ENV["RUBYLIB"] not set and bundler not on load path' do
      ENV['RUBYLIB'] = nil
      @util.rubylib_with_bundler([]).should be_empty
    end

    it 'should not include ENV["RUBYLIB"] if not set' do
      ENV['RUBYLIB'] = nil
      @util.rubylib_with_bundler(['/some/bundler/path']).should == '/some/bundler/path'
    end

    it 'should only include load path entries containing bundler' do
      ENV['RUBYLIB'] = '/path/to/abc'
      load_path = ['/some/bundler/path', '/some/other/path']
      @util.rubylib_with_bundler(load_path).should =='/path/to/abc:/some/bundler/path'
    end
  end
end
