
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "Dir extensions for VFS" do

  describe "with vfszip urls" do
    it "should allow globbing within archives with explicit vfszip" do
      base = "vfszip:#{Dir.pwd}/#{TEST_DATA_DIR}/home/larry/archive1.jar"
      pattern = "#{base}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include File.join( base, 'web.xml' )
      items.should include File.join( base, 'lib' )
    end
 
    it "should allow globbing within nested archives with explicit vfszip" do
      base = "vfszip:#{Dir.pwd}/#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar"
      pattern = "#{base}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include "#{base}/manifest.txt"
    end
  end
  
  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      prefix = ''
      prefix = "#{Dir.pwd}/" if style==:absolute

      it "should allow globbing without any special globbing characters on normal files" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/" )
      end

      it "should allow globbing without any special globbing characters for archives" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar" )
      end

      it "should allow globbing without any special globbing characters within archives" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" )
      end

      it "should allow globbing without any special globbing characters for nested archives" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar" )
      end

      it "should allow globbing without any special globbing characters for within archives" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
      end

      it "should allow appropriate globbing of normal files" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/*" )
        items.should_not be_empty
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" )
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/file2.txt" )
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar" )
      end
  
      it "should determine if VFS is needed for archives" do
        base = "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar"
        items = Dir.glob( "#{base}/*" )
        items.should_not be_empty
      end
  
      it "should determine if VFS is needed for nested archives" do
        base = "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar"
        items = Dir.glob( "#{base}/*" )
        items.should_not be_empty
        items.should include( "#{base}/manifest.txt" )
      end
  
      it "should determine if VFS is needed with relative paths" do
        base = "#{prefix}#{TEST_DATA_BASE}/home/larry/archive1.jar/lib/archive2.jar"
        items = Dir.glob( "#{base}/*" )
        items.should_not be_empty
        items.should include( "#{base}/manifest.txt" )
      end

      it "should allow alternation globbing on normal files" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/{larry}/file{,1,2}.{txt}" )
        items.should_not be_empty
        items.size.should eql 2
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" )
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/file2.txt" )
      end

      it "should allow alternation globbing within archives" do
        items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive*{.zip,.jar,.ear}" )
        items.should_not be_empty
        items.size.should eql 3
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive3.ear" )
        items.should include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive4.zip" )
        items.should_not include( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive4.txt" )
      end
    end
  end

end
