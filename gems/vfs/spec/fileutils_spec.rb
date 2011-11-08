require File.dirname(__FILE__) + '/spec_helper.rb'
require 'fileutils'

describe "fileutils compatibility with VFS" do

  extend TestDataCopyHelper

  it "should support fileutils.cp for vfs paths" do
    prefix = test_copy_base_path( :vfs )
    source = File.join( prefix, "home", "larry", "file1.txt" )
    destination = File.join( prefix, "home", "larry", "file1_copy.txt" )
    FileUtils.cp( source, destination )
    File.exist?( destination ).should be_true
  end
end
