
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "IO extensions for VFS" do

  extend TestDataHelper

  it "should allow reading of full VFS URLs" do
    content = IO.read( "vfs:#{TEST_DATA_DIR}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" ).chomp
    content.should_not be_nil
    content.should_not be_empty
    content.should eql( "This is manifest.txt" )
  end

  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      case ( style )
        when :relative
          prefix = "./#{TEST_DATA_BASE}"
        when :absolute
          prefix = File.expand_path( File.join( File.dirname( __FILE__ ), '..', TEST_DATA_BASE ) )
      end

      it "should allow reading of regular files" do
        content = IO.read( "#{prefix}/home/larry/file1.txt" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is file 1" )
      end
    
      it "should allow reading of files within an archive" do
        content = IO.read( "#{prefix}/home/larry/archive1.jar/web.xml" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is web.xml" )
      end
    
      it "should allow reading of files within a nested archive" do
        content = IO.read( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" ).chomp
        content.should_not be_nil
        content.should_not be_empty
        content.should eql( "This is manifest.txt" )
      end
    end
  end

end
