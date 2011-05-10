
require File.dirname(__FILE__) + '/spec_helper.rb'
require 'pathname'

describe "Pathname extensions for VFS" do

  describe "realpath" do
    it "should expand VFS paths" do
      pathname = Pathname.new("vfs:/tmp/test")
      pathname.should_receive(:expand_path).and_return(Pathname.new("/expanded/path"))
      pathname.realpath.to_s.should == "/expanded/path"
    end

    it "should find real path for non-VFS paths" do
      pathname = Pathname.new("/tmp/test")
      pathname.should_receive(:realpath_without_vfs).and_return(Pathname.new("/real/path"))
      pathname.realpath.to_s.should == "/real/path"
    end
  end
end
