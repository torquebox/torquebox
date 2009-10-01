
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "IO extensions for VFS" do

  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      prefix = ''
      prefix = "#{Dir.pwd}/" if style==:absolute

      it "should allow reading of regular files" do
        content = IO.read( "#{prefix}#{TEST_DATA_DIR}/home/larry/file1.txt" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is file 1" )
      end
    
      it "should allow reading of files within an archive" do
        content = IO.read( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/web.xml" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is web.xml" )
      end
    
      it "should allow reading of files within a nested archive" do
        content = IO.read( "#{prefix}#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is manifest.txt" )
      end
    end
  end

end
