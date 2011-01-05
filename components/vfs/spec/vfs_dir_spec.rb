
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "VFS::Dir" do

  extend TestDataHelper

  describe "entries" do
    it "should find vfs entries outside of archives" do
      path = "#{archive1_path}/.."
      ::Dir.new( path ).entries.should == VFS::Dir.new( vfs_path(path) ).entries
    end

    it "should find vfs entries inside of archives" do
      path = "#{archive1_vfs_path}/other_lib/subdir"
      entries = VFS::Dir.new( path ).entries
      entries.size.should == 3
      entries.should include( "." )
      entries.should include( ".." )
      entries.should include( "archive6.jar" )
    end
  end
end
