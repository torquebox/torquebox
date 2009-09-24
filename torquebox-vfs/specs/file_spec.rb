
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "File extensions for VFS" do

  it "should provide mtime for normal files" do
    mtime = File.mtime( "#{TEST_DATA_DIR}/home/larry/file1.txt" )
    mtime.should_not be_nil
  end

  it "should provide mtime for files in an archive" do
    mtime = File.mtime( "#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" )
    mtime.should_not be_nil
  end

  it "should provide mtime for files in a nested archive" do
    mtime = File.mtime( "#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
    mtime.should_not be_nil
  end

  it "should test existance of normal files" do
    File.exist?( "#{TEST_DATA_DIR}/home/larry/file1.txt" ).should be_true
    File.exist?( "#{TEST_DATA_DIR}/home/larry/file42.txt" ).should be_false
  end

  it "should test existance of files in an archive" do
    File.exist?( "#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" ).should be_true
  end

  it "should test directoryness for normal files" do
    File.exist?( "#{TEST_DATA_DIR}/home/larry" ).should be_true
    File.exist?( "#{TEST_DATA_DIR}/home/larry/file1.txt" ).should be_false
  end

end
