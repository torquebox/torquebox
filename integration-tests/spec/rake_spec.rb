require 'spec_helper'

describe "rake tasks" do

  before(:all) do
    ENV['TORQUEBOX_HOME'] = File.join(File.dirname(__FILE__), '..', 'target', 'integ-dist')
    ENV['JBOSS_HOME'] = "#{ENV['TORQUEBOX_HOME']}/jboss"
  end

  it "should pass a sanity check" do
    output = rake('integ:sanity_check')
    output.should include('sanity check passed')
  end

  describe "TorqueBox::DeployUtils" do
    it "should use a valid cluster config file" do
      full_path = File.join(TorqueBox::DeployUtils.config_dir, TorqueBox::DeployUtils.cluster_config_file)
      File.exist?(full_path).should be_true
    end
  end

  describe "torquebox:archive" do

    it "should create archives" do
      Dir.chdir( root_dir ) do
        output = rake('torquebox:archive')
        output.should include('Created archive')
        output.should include('basic.knob')
      end
      File.delete( "#{root_dir}/basic.knob" )
    end

    it "should create an archive with a name supplied on the command line" do
      Dir.chdir( root_dir ) do
        output = rake('torquebox:archive[foo]')
        output.should include('Created archive')
        output.should include('foo.knob')
      end
      File.delete( "#{root_dir}/foo.knob" )
    end

    it "should create an archive with a name supplied as an env variable" do
      Dir.chdir( root_dir ) do
        output = rake('torquebox:archive NAME=foo')
        output.should include('Created archive')
        output.should include('foo.knob')
      end
      File.delete( "#{root_dir}/foo.knob" )
    end

  end

  describe "torquebox:deploy and torquebox:undeploy" do

    it "should do a basic yaml deployment" do
      Dir.chdir( root_dir ) do
        check_deployment( 'torquebox:deploy' )
        check_undeployment( 'torquebox:undeploy' )
      end
    end

    it "should do a yaml deployment with a context supplied on the command line" do
      Dir.chdir( root_dir ) do
        check_deployment( 'torquebox:deploy[/foo]' )
        contents = File.read("#{TorqueBox::DeployUtils.deploy_dir}/basic-knob.yml")
        contents.should include('context: /foo')
        check_undeployment( 'torquebox:undeploy' )
      end
    end

    it "should do a yaml deployment with a name supplied on the command line" do
      Dir.chdir( root_dir ) do
        check_deployment( 'torquebox:deploy[/foo,foo]', 'foo' )
        contents = File.read("#{TorqueBox::DeployUtils.deploy_dir}/foo-knob.yml")
        contents.should include('context: /foo')
        check_undeployment( 'torquebox:undeploy[foo]', 'foo' )
      end
    end

    it "should do a yaml deployment with a name supplied as an env variable" do
      Dir.chdir( root_dir ) do
        check_deployment( 'torquebox:deploy NAME=foo', 'foo' )
        check_undeployment( 'torquebox:undeploy NAME=foo', 'foo' )
      end
    end

    it "should treat undeployment of a non-existent yaml deployment as a noop" do
      Dir.chdir( root_dir ) do
        output = rake('torquebox:undeploy NAME=moleman')
        output.should include("Can't undeploy #{TorqueBox::DeployUtils.deploy_dir}/moleman-knob.yml. It does not appear to be deployed.")
        output.should include("Can't undeploy #{TorqueBox::DeployUtils.deploy_dir}/moleman.knob. It does not appear to be deployed.")
        output.should include("Nothing to undeploy")
      end
    end

  end

  describe "torquebox:deploy:archive and torquebox:undeploy:archive" do

    it "should do a basic archive deployment" do
      Dir.chdir(root_dir) do
        check_deployment( 'torquebox:deploy:archive', 'basic', '.knob' )
        check_undeployment('torquebox:undeploy:archive', 'basic', '.knob' )
        File.exist?("#{root_dir}/basic.knob").should == true
        FileUtils.rm_rf("#{root_dir}/basic.knob")
      end
    end

    it "should do a archive deployment with a name supplied on the command line" do
      Dir.chdir(root_dir) do
        check_deployment( 'torquebox:deploy:archive[baz]', 'baz', '.knob' )
        check_undeployment('torquebox:undeploy:archive[baz]', 'baz', '.knob' )
        File.exist?("#{root_dir}/baz.knob").should == true
        FileUtils.rm_rf("#{root_dir}/baz.knob")
      end
    end

    it "should do a archive deployment with a name supplied as an env variable" do
      Dir.chdir(root_dir) do
        check_deployment( 'torquebox:deploy:archive NAME=joe', 'joe', '.knob' )
        check_undeployment('torquebox:undeploy:archive NAME=joe', 'joe', '.knob' )
        File.exist?("#{root_dir}/joe.knob").should == true
        FileUtils.rm_rf("#{root_dir}/joe.knob")
      end
    end

    it "should treat undeployment of a non-existent archive deployment as a noop" do
      Dir.chdir( root_dir ) do
        output = rake('torquebox:undeploy:archive NAME=poochie')
        output.should include("Can't undeploy #{TorqueBox::DeployUtils.deploy_dir}/poochie.knob. It does not appear to be deployed.")
        output.should include("Nothing to undeploy")
      end
    end

    it "should be able to undeploy an archive using only torquebox:undeploy" do
      Dir.chdir( root_dir ) do
        check_deployment( 'torquebox:deploy:archive NAME=joe', 'joe', '.knob' )
        check_undeployment('torquebox:undeploy NAME=joe', 'joe', '.knob' )
        File.exist?("#{root_dir}/joe.knob").should == true
        FileUtils.rm_rf("#{root_dir}/joe.knob")
      end
    end

  end

  def rake(task)
    integ_jruby("-S rake -f #{File.dirname(__FILE__)}/../apps/rails3.1/basic/Rakefile #{task} --trace")
  end

  private

    def check_deployment(rake_command, name = 'basic', suffix = '-knob.yml')
      output = rake(rake_command)
      output.should include("Deployed: #{name}#{suffix}")
      output.should include("into: #{TorqueBox::DeployUtils.deploy_dir}")
      File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}").should == true
      File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}.dodeploy").should == true
    end

    def check_undeployment(rake_command, name = 'basic', suffix = '-knob.yml')
      output = rake(rake_command)
      output.should include("Undeployed: #{name}#{suffix}")
      output.should include("from: #{TorqueBox::DeployUtils.deploy_dir}")

      # give the AS as many as five seconds to undeploy
      5.times {
        break unless File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}") 
        puts "Waiting for undeployment..."
        sleep 1
      }

      File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}").should == false
      File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}.dodeploy").should == false
      # sleep for a few more seconds to ensure things are actually
      # undeployed inside AS7 vs the undeployment simply being picked
      # up by the deployment scanner
      sleep 3
    end

  def root_dir
    File.join( File.dirname(__FILE__), '..', 'apps', 'rails3.1', 'basic' )
  end

end

