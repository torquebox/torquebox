
require 'fileutils'

require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "Dir extensions for VFS" do

  extend PathHelper
  extend TestDataHelper

  describe "with vfs urls" do
    it "should allow globbing within archives with explicit vfs" do
      pattern = "#{archive1_vfs_path}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include File.join( "#{archive1_vfs_path}", 'web.xml' )
      items.should include File.join( "#{archive1_vfs_path}", 'lib' )
    end

    it "should allow globbing within nested archives with explicit vfs" do
      pattern = "#{archive2_vfs_path}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include "#{archive2_vfs_path}/manifest.txt"
    end

    it "should create new Dirs" do
      lambda {
        Dir.new("#{archive2_vfs_path}")
      }.should_not raise_error
    end
  end

  [ :absolute, :relative, :vfs ].each do |style|
    describe "with #{style} paths" do

      case ( style )
        when :relative
          prefix = test_data_base_path( :relative )
        when :absolute
          prefix = test_data_base_path( :absolute )
        when :vfs
          prefix = test_data_base_path( :vfs )
      end

      it "should ignore dotfiles by default" do
        glob_pattern = "#{prefix}/dotfiles/*"
        items = Dir.glob( glob_pattern )
        items.should_not be_empty
        items.size.should eql(3)
        items.should include( "#{prefix}/dotfiles/one" )
        items.should include( "#{prefix}/dotfiles/three" )
        items.should include( "#{prefix}/dotfiles/foo.txt" )
      end

      it "should match dotfiles if explicitly asked" do
        items = Dir.glob( "#{prefix}/dotfiles/.*" )
        items.should_not be_empty
        items.size.should eql(2)
        items.should include( "#{prefix}/dotfiles/.two" )
        items.should include( "#{prefix}/dotfiles/.four" )
      end

      it "should allow globbing without any special globbing characters on normal files" do
        items = Dir.glob( "#{prefix}/home/larry" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry" )
      end

      it "should allow globbing without any special globbing characters on a single normal file" do
        items = Dir.glob( "#{prefix}/home/larry/file1.txt" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/file1.txt" )
      end

      it "should allow globbing without any special globbing characters for archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/archive1.jar" )
      end

      it "should allow globbing without any special globbing characters within archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/web.xml" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/archive1.jar/web.xml" )
      end

      it "should allow globbing without any special globbing characters for nested archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar" )
      end

      it "should allow globbing without any special globbing characters for within archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
      end

      it "should provide access to entries" do
        path = "#{prefix}/home/larry" 
        items = Dir.entries( path )
        items.should_not be_empty
        items.size.should eql( 5 )
        items.should include( "." )
        items.should include( ".." )
        items.should include( "file1.txt" )
        items.should include( "file2.txt" )
        items.should include( "archive1.jar" )
      end

      it "should provide iteration over its entries" do
        items = []
        Dir.foreach( "#{prefix}/home/larry" ) do |e|
          items << e
        end

        items.should_not be_empty
        items.size.should eql( 5 )
        items.should include( "." )
        items.should include( ".." )
        items.should include( "file1.txt" )
        items.should include( "file2.txt" )
        items.should include( "archive1.jar" )
      end

      it "should allow appropriate globbing of normal files" do
        items = Dir.glob( "#{prefix}/home/larry/*" )
        items.should_not be_empty
        items.should include( "#{prefix}/home/larry/file1.txt" )
        items.should include( "#{prefix}/home/larry/file2.txt" )
        items.should include( "#{prefix}/home/larry/archive1.jar" )
      end

      it "should determine if VFS is needed for archives" do
        items = Dir.glob( "#{@archive1_path}/*" )
        items.should_not be_empty
      end

      it "should determine if VFS is needed for nested archives" do
        base = "#{prefix}/home/larry/archive1.jar/lib/archive2.jar"
        items = Dir.glob( "#{base}/*" )
        items.should_not be_empty
        items.should include( "#{base}/manifest.txt" )
      end

      it "should determine if VFS is needed with relative paths" do
        base = "#{prefix}/home/larry/archive1.jar/lib/archive2.jar"
        items = Dir.glob( "#{base}/*" )
        items.should_not be_empty
        items.should include( "#{base}/manifest.txt" )
      end

      it "should allow character-class matching" do
        items = Dir.glob( "#{prefix}/home/{larry}/file[12].{txt}" )
        items.should_not be_empty
        items.size.should eql 2
        items.should include( "#{prefix}/home/larry/file1.txt" )
        items.should include( "#{prefix}/home/larry/file2.txt" )
      end

      it "should allow alternation globbing on normal files" do
        items = Dir.glob( "#{prefix}/home/{larry}/file{,1,2}.{txt}" )
        items.should_not be_empty
        items.size.should eql 2
        items.should include( "#{prefix}/home/larry/file1.txt" )
        items.should include( "#{prefix}/home/larry/file2.txt" )
      end

      it "should allow alternation globbing within archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/lib/archive*{.zip,.jar,.ear}" )
        items.should_not be_empty
        items.size.should eql 3
        items.should     include( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should     include( "#{prefix}/home/larry/archive1.jar/lib/archive3.ear" )
        items.should     include( "#{prefix}/home/larry/archive1.jar/lib/archive4.zip" )
        items.should_not include( "#{prefix}/home/larry/archive1.jar/lib/archive4.txt" )
      end

      it "should allow alternation globbing with trailing comma" do
        items = Dir.glob( "#{prefix}/home/todd/index{.en,}{.html,}{.erb,.haml,}" )
        items.should_not be_empty
        items.size.should eql 4
        items.should     include( "#{prefix}/home/todd/index.html.erb" )
        items.should     include( "#{prefix}/home/todd/index.en.html.erb" )
        items.should     include( "#{prefix}/home/todd/index.html.haml" )
        items.should     include( "#{prefix}/home/todd/index.en.html.haml" )
      end

      it "should allow alternation globbing with internal globs" do
        items = Dir.glob( "#{prefix}/home/{todd/*,larry/*}{.haml,.txt,.jar}" )
        items.should_not be_empty
        items.should     include( "#{prefix}/home/todd/index.html.haml" )
        items.should     include( "#{prefix}/home/todd/index.en.html.haml" )
        items.should     include( "#{prefix}/home/larry/file1.txt" )
        items.should     include( "#{prefix}/home/larry/file2.txt" )
        items.should     include( "#{prefix}/home/larry/archive1.jar" )
      end

      it "should allow for double-star globbing within archives" do
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/**/*.jar" )
        items.should_not be_empty
        #items.size.should eql 1
        items.should     include( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should     include( "#{prefix}/home/larry/archive1.jar/other_lib/subdir/archive6.jar" )
        items.should_not include( "#{prefix}/home/larry/archive1.jar/lib/archive4.txt" )
      end

      it "should create new Dirs" do
        lambda {
          Dir.new(prefix)
        }.should_not raise_error
      end

    end
  end

  describe "mkdir" do
    it "should mkdir inside vfs archive when directory mounted on filesystem" do
      FileUtils.rm_rf "target/mnt"
      File.exists?("target/mnt").should_not be_true # catches a 1.9 issue 
      archive = org.jboss.vfs::VFS.child( @archive1_path )
      logical = archive.getChild( "lib" )
      physical = java.io::File.new( "target/mnt" )
      physical.mkdirs
      mount = org.jboss.vfs::VFS.mountReal( physical, logical )
      begin
        lambda {
          Dir.mkdir("#{@archive1_path}/lib/should_mkdir_inside_vfs_archive")
          File.directory?("target/mnt/should_mkdir_inside_vfs_archive").should be_true
        }.should_not raise_error
      ensure
        mount.close
      end
    end
  end

  describe "chdir" do
    it "should require a block be passed" do
      lambda {
        Dir.chdir("/tmp")
      }.should raise_error
    end
    it "should work for vfs paths" do
      pwd = Dir.pwd
      dir = Dir.chdir( test_data_base_path(:vfs) ) { Dir.pwd }
      dir.should == test_data_base_path(:absolute)
      pwd.should == Dir.pwd
    end

    xit "should work for home dirs" do
      pwd = Dir.pwd
      dir = Dir.chdir { Dir.pwd }
      dir.downcase.should == ENV['HOME'].downcase
      pwd.should == Dir.pwd
    end
  end

end
