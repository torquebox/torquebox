
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "File extensions for VFS" do

  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      prefix = ''
      prefix = "#{Dir.pwd}/" if style==:absolute

      it "should provide mtime for normal files" do
        mtime = File.mtime( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" )
        mtime.should_not be_nil
      end

      it "should allow stat for normal files" do
        file = "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" 
        stat = File.stat( file )
        stat.should_not be_nil
        stat.mtime.should eql( File.mtime( file ) )
      end

      it "should provide mtime for files in an archive" do
        mtime = File.mtime( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" )
        mtime.should_not be_nil
      end

      it "should allow stat for files in an archive" do
        file = "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml"
        stat = File.stat( file )
        stat.should_not be_nil
        stat.mtime.should eql( File.mtime( file ) )
      end
    
      it "should provide mtime for files in a nested archive" do
        mtime = File.mtime( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
        mtime.should_not be_nil
      end
    
      it "should test existance of normal files" do
        File.exist?( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" ).should be_true
        File.exist?( "#{prefix}#{TEST_DATA_DIR}/home/larry/file42.txt" ).should be_false
      end
    
      it "should test existance of files in an archive" do
        File.exist?( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" ).should be_true
      end
    
      it "should test directoryness for normal files" do
        File.directory?( "#{prefix}#{TEST_DATA_DIR}/home/larry" ).should be_true
        File.directory?( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" ).should be_false
      end
    end
  end
end
