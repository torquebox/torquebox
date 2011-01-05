
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "VFS::Dir" do

  extend TestDataHelper

  describe "entries" do
    it "should find vfs entries outside of archives" do
      path = "#{@archive1_path}/.."
      ::Dir.new( path ).entries.should == VFS::Dir.new( "vfs:#{path}" ).entries
    end

    it "should find vfs entries inside of archives" do
      path = "vfs:#{@archive1_path}/other_lib/subdir"
      entries = VFS::Dir.new( path ).entries
      entries.size.should == 1
      entries.first.should == "archive6.jar"
    end
  end
end
