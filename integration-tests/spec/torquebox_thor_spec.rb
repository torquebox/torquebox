require 'spec_helper'

describe "torquebox thor utility tests" do

  before(:all) do
    ENV['TORQUEBOX_HOME'] = File.join(File.dirname(__FILE__), '..', 'target', 'integ-dist')
    ENV['JBOSS_HOME'] = "#{ENV['TORQUEBOX_HOME']}/jboss"
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
  
  private
  
  def check_deployment(tb_command, name = 'basic', suffix = '-knob.yml')
    output = tb(tb_command)
<<<<<<< HEAD
=======
    puts "\nFor command #{tb_command}, deploy output is: "
    puts output
>>>>>>> b4bf1f6dcf48d3620d48e6e28a2d668c8add281a
    output.should include("Deployed: #{name}#{suffix}")
    output.should include("into: #{TorqueBox::DeployUtils.deploy_dir}")
    File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}").should == true
    File.exist?("#{TorqueBox::DeployUtils.deploy_dir}/#{name}#{suffix}.dodeploy").should == true
  end
  
  def check_undeployment(tb_command, name = 'basic', suffix = '-knob.yml')
    output = tb(tb_command)
    puts "\nFor command #{tb_command}, output is: "
    puts output
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
  end  
  
  def root_dir
    File.join( File.dirname(__FILE__), '..', 'apps', 'rails3.1', 'basic' )
  end
  
  def tb(cmd)
    integ_jruby("-S torquebox #{cmd}")
  end  

<<<<<<< HEAD
end
=======
end
>>>>>>> b4bf1f6dcf48d3620d48e6e28a2d668c8add281a
