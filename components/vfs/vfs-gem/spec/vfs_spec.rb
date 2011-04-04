require File.dirname(__FILE__) + '/spec_helper.rb'


require 'pathname'

describe "VFS path resolution" do

  extend PathHelper

  describe "resolve_within_archive" do
    it "should return pathnames with vfs: prefix unmodified" do
      pathname = Pathname.new("vfs:/tmp/foo")
      path = TorqueBox::VFS.resolve_within_archive(pathname)
      path.should == pathname.to_s
    end
  end

  describe "resolve_path_url" do
    it "should prefix relative paths with the current dir" do
      cwd = Dir.pwd
      path = TorqueBox::VFS.resolve_path_url( "foo/bar" )
      path.should match /^#{vfs_path(cwd)}\/foo\/bar$/
    end

    it "should not prefix absolute paths with the current dir" do
      path = TorqueBox::VFS.resolve_path_url( "/foo/bar" )
      path.should match /^vfs:\/foo\/bar$/
    end

    it "should treat paths with windows drive letters as absolute" do
      path = TorqueBox::VFS.resolve_path_url( "C:/foo/bar" )
      path.should match /^vfs:\/C:\/foo\/bar$/
    end
  end

end
