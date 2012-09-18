require 'spec_helper'
require 'fileutils'

describe "torquebox thor utility tests" do

  before(:all) do
    ENV['TORQUEBOX_HOME'] = File.join(File.dirname(__FILE__), '..', 'target', 'integ-dist')
    ENV['JBOSS_HOME'] = "#{ENV['TORQUEBOX_HOME']}/jboss"
  end

  describe "torquebox archive" do

    it "should archive an app from the root" do
      Dir.chdir( root_dir ) do
        tb('archive')
        File.exist?("#{root_dir}/basic.knob").should == true
        FileUtils.rm_rf("#{root_dir}/basic.knob")
      end
    end

    it "should archive an app with a root specified" do
      tb("archive #{root_dir}")
      File.exist?('basic.knob').should == true
      FileUtils.rm_rf('basic.knob')
    end

    it "should archive and deploy an app from the root" do
      Dir.chdir( root_dir ) do
        check_deployment("archive --deploy", 'basic', '.knob')
        File.exist?("#{root_dir}/basic.knob").should == true
        FileUtils.rm_rf "#{root_dir}/basic.knob"
        check_undeployment('undeploy', 'basic', '.knob')
      end
    end

    it "should archive and deploy an app with a root specified" do
      check_deployment("archive #{root_dir} --deploy", 'basic', '.knob')
      File.exist?('basic.knob').should == true
      FileUtils.rm_rf('basic.knob')
      Dir.chdir( root_dir ) do
        check_undeployment("undeploy", 'basic', '.knob')
      end
    end

  end

  describe "torquebox deploy" do

    it "should deploy a basic app" do
      Dir.chdir( root_dir ) do
        check_deployment "deploy"
        check_undeployment "undeploy"
      end
    end

    it "should deploy an app with a name specified on the command line" do
      Dir.chdir( root_dir ) do
        check_deployment( "deploy --name=foobedoo", 'foobedoo' )
        check_undeployment( "undeploy --name=foobedoo", 'foobedoo')
      end
    end

    it "should deploy an app with a context path specified on the command line" do
      Dir.chdir( root_dir ) do
        check_deployment 'deploy --context_path=/leftorium'
        contents = File.read("#{TorqueBox::DeployUtils.deploy_dir}/basic-knob.yml")
        contents.should include('context: /leftorium')
        check_undeployment 'undeploy'
      end
    end

    it "should deploy an app with an environment specified on the command line" do
      Dir.chdir( root_dir ) do
        check_deployment 'deploy --env=production'
        contents = File.read("#{TorqueBox::DeployUtils.deploy_dir}/basic-knob.yml")
        contents.should include('production')
        check_undeployment 'undeploy'
      end
    end

    it "should deploy an app with a root specified on the command line" do
      check_deployment "deploy #{root_dir}"
      Dir.chdir( root_dir ) do
        check_undeployment "undeploy"
      end
    end

  end

  # Disabled on Windows because it pops up a cmd.exe dialog that must
  # be manually closed on the CI machine for the test to continue.
  unless TESTING_ON_WINDOWS
    describe "torquebox run" do
      it "should pass JVM options specified on the command line" do
        output = tb( 'run -J \"-Xmx384m -Dmy.property=value\" --extra \"\--version\"' )
        output.should match( /\s+JAVA_OPTS: .* -Xmx384m -Dmy\.property=value/ )
      end
    end
  end

  describe "torquebox rails" do
    before(:each) do
      ENV['RAILS_VERSION'] = '>3.2'
      @app_dir = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps', 'torquebox_thor_spec_app' )
    end

    after(:each) do
      ENV['RAILS_VERSION'] = nil
      FileUtils.rm_rf( @app_dir )
    end

    it "should create the app and its directory" do
      tb( "rails #{@app_dir}" )
      check_app_dir
    end

    it "should create the app even if its directory already exists" do
      FileUtils.mkdir_p( @app_dir )
      Dir.chdir( @app_dir ) do
        tb( 'rails' )
      end
      check_app_dir
    end

    it "should modify the app if it already exists" do
      rails( ENV['RAILS_VERSION'], "new #{@app_dir}" )
      File.exist?( File.join( @app_dir, 'Gemfile' ) ).should be_true
      File.read( File.join( @app_dir, 'Gemfile' ) ).should_not include( 'torquebox' )
      tb( "rails #{@app_dir}" )
      check_app_dir
    end

    it "should modify the app in the current directory if it already exists" do
      rails( ENV['RAILS_VERSION'], "new #{@app_dir}" )
      File.exist?( File.join( @app_dir, 'Gemfile' ) ).should be_true
      File.read( File.join( @app_dir, 'Gemfile' ) ).should_not include( 'torquebox' )
      Dir.chdir( @app_dir ) do
        tb( 'rails' )
      end
      check_app_dir
    end

    it "should create a rails 2.3 app and its directory" do
      # 2.3 will automatically get chosen if we don't specify, and
      # this ensures things work without explicitly setting
      # RAILS_VERSION
      ENV['RAILS_VERSION'] = nil
      tb( "rails #{@app_dir}" )
      File.exist?( @app_dir ).should be_true
      File.exist?( File.join( @app_dir, 'config', 'environment.rb' ) ).should be_true
      File.read( File.join( @app_dir, 'config', 'environment.rb' ) ).should include( 'torquebox' )
    end

    def check_app_dir
      File.exist?( @app_dir ).should be_true
      File.exist?( File.join( @app_dir, 'Gemfile' ) ).should be_true
      File.read( File.join( @app_dir, 'Gemfile' ) ).should include( 'torquebox' )
    end

    def rails( version, cmd )
      rails = File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist',
                         'jruby', 'lib', 'ruby', 'gems', '1.8', 'bin', 'rails' )
      integ_jruby( "#{rails} _#{version}_ #{cmd}" )
    end
  end

  private

  def check_deployment(tb_command, name = 'basic', suffix = '-knob.yml')
    output = tb(tb_command)
    output.should include("Deployed: #{name}#{suffix}")
    deployment = "#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}"
    dodeploy = "#{deployment}.dodeploy"
    isdeploying = "#{deployment}.isdeploying"
    deployed = "#{deployment}.deployed"
    File.exist?(deployment).should == true
    (File.exist?(dodeploy) || File.exist?(isdeploying) || File.exist?(deployed)).should == true
  end

  def check_undeployment(tb_command, name = 'basic', suffix = '-knob.yml')
    output = tb(tb_command)
    output.should include("Undeployed: #{name}#{suffix}")

    # give the AS as many as five seconds to undeploy
    5.times {
      break unless File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}")
      puts "Waiting for undeployment..."
      sleep 1
    }

    File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}").should == false
    File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}.dodeploy").should == false
    output
  end

  def root_dir
    File.join( File.dirname(__FILE__), '..', 'apps', 'rails3.1', 'basic' )
  end

  def tb(cmd)
    integ_jruby("-S torquebox #{cmd}")
  end

end
