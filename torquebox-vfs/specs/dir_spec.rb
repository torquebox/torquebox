
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "Dir extensions for VFS" do

  it "should allow appropriate globbing of normal files" do
    items = Dir.glob( "#{TEST_DATA_DIR}/home/larry/*" )
    items.should_not be_empty
    items.should include( "#{TEST_DATA_DIR}/home/larry/file1.txt" )
    items.should include( "#{TEST_DATA_DIR}/home/larry/file2.txt" )
    items.should include( "#{TEST_DATA_DIR}/home/larry/archive1.jar" )
  end

  it "should allow globbing within archives" do
    base = "vfszip://#{Dir.pwd}/#{TEST_DATA_DIR}/home/larry/archive1.jar/"
    pattern = "#{base}/*"
    items = Dir.glob( pattern )
    items.should_not be_empty
    items.should include "#{base}/web.xml"
    items.should include "#{base}/lib"
  end

  it "should allow globbing within nested archives" do
    base = "vfszip://#{Dir.pwd}/#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar"
    pattern = "#{base}/*"
    items = Dir.glob( pattern )
    items.should_not be_empty
    items.should include "#{base}/manifest.txt"
  end

  it "should determine if VFS is needed for archives" do
    base = "#{TEST_DATA_DIR}/home/larry/archive1.jar"
    items = Dir.glob( "#{base}/*" )
    items.should_not be_empty
  end

  it "should determine if VFS is needed for nested archives" do
    base = "#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar"
    items = Dir.glob( "#{base}/*" )
    items.should_not be_empty
  end

  it "should determine if VFS is needed with relative paths" do
    base = "#{TEST_DATA_BASE}/home/larry/archive1.jar/lib/archive2.jar"
    items = Dir.glob( "#{base}/*" )
    items.should_not be_empty
    items.should include( "#{base}/manifest.txt" )
  end

end
