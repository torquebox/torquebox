require 'torquebox/server'

describe TorqueBox::Server do

  context "jruby_home" do
    it "should expand relative paths" do
      java.lang.System.setProperty('jruby.home', '.')
      TorqueBox::Server.jruby_home.should == Dir.pwd
    end
  end

  context "torquebox_home" do
    it "should return nil if torquebox-server is not installed" do
      TorqueBox::Server.torquebox_home.should == nil
    end

    it "should use torquebox-server gem's installed path" do
      server_gem = mock('server-gem')
      Gem::Specification.should_receive(:find_by_name).with('torquebox-server').and_return(server_gem)
      server_gem.stub('full_gem_path').and_return('torquebox-server-install-path')
      TorqueBox::Server.torquebox_home.should == 'torquebox-server-install-path'
    end
  end
end
