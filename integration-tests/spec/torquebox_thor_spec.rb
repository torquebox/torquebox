require 'spec_helper'

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

  describe "torquebox run" do
    it "should pass JVM options specified on the command line" do
      output = tb( 'run -J \"-Xmx384m -Dmy.property=value\" --extra \"\--version\"' )
      output.should match( /\s+JAVA_OPTS: .* -Xmx384m -Dmy\.property=value/ )
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
