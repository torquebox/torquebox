# Make Thor's start a no-op
require 'thor'
class Thor
  def self.start
  end
end

load File.expand_path(File.join(File.dirname(__FILE__), '..', 'bin', 'torquebox'))

describe TorqueBoxCommand do
  before(:each) do
    @torquebox = TorqueBoxCommand.new
  end

  context "jruby_home" do
    it "should expand relative paths" do
      java.lang.System.setProperty('jruby.home', '.')
      @torquebox.jruby_home.should == Dir.pwd
    end
  end

  context "torquebox_home" do
    it "should expand relative paths" do
      @torquebox.torquebox_home.should_not include("..")
    end
  end
end
