
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "IO extensions for VFS" do

  extend TestDataHelper

  [ :absolute, :relative ].each do |style|
    [ :vfs, :normal ].each do |vfs_style|
      describe "with #{vfs_style} #{style} paths" do
        
        prefix = test_data_base_path( style )
        prefix = vfs_path( prefix ) if vfs_style == :vfs

        describe 'read' do
          
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

          it "should honor the length argument" do
            content = IO.read( "#{prefix}/home/larry/file1.txt", 1 ).chomp
            content.should_not be_nil
            content.should_not be_empty
            content.should eql( "T" )
          end

          it "should honor the offset argument" do
            content = IO.read( "#{prefix}/home/larry/file1.txt", 1, 1 ).chomp
            content.should_not be_nil
            content.should_not be_empty
            content.should eql( "h" )
          end

        end
        
        describe 'readline' do
          
          it "should allow reading a regular file" do
            content = IO.readlines( "#{prefix}/home/larry/file1.txt" )
            content.should_not be_nil
            content.should_not be_empty
            content.first.chomp.should eql( "This is file 1" )
          end

          it "should allow reading of files within an archive" do
            content = IO.readlines( "#{prefix}/home/larry/archive1.jar/web.xml" )
            content.should_not be_nil
            content.should_not be_empty
            content.first.chomp.should eql( "This is web.xml" )
          end

          it "should allow reading of files within a nested archive" do
            content = IO.readlines( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
            content.should_not be_nil
            content.should_not be_empty
            content.first.chomp.should eql( "This is manifest.txt" )
          end

        end
        
      end
    end
  end
end
