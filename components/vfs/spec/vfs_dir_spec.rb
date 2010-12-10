
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "VFS::Dir" do

  before(:each) do
    @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
    @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )
    @archive1_path = File.expand_path( "#{TEST_DATA_DIR}/home/larry/archive1.jar" )
    @archive1_file = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_mount_point = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_handle = org.jboss.vfs::VFS.mountZip( @archive1_file, @archive1_mount_point, @temp_file_provider )
  end

  after(:each) do
    @archive1_handle.close
  end

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
