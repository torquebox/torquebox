require 'torquebox/server'

describe TorqueBox::Server do

  context "jruby_home" do
    it "should expand relative paths" do
      java.lang.System.setProperty('jruby.home', '.')
      TorqueBox::Server.jruby_home.should == Dir.pwd
    end
  end

  context "torquebox_home" do
    it "should expand relative paths" do
      TorqueBox::Server.torquebox_home.should_not include("..")
    end
  end
end
