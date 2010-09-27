
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "Dir extensions for VFS" do

  before(:each) do
    @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
    @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )

    @archive1_path = File.expand_path( "#{TEST_DATA_DIR}/home/larry/archive1.jar" )
    @archive1_file = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_mount_point = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_handle = org.jboss.vfs::VFS.mountZip( @archive1_file, @archive1_mount_point, @temp_file_provider )

    @archive2_path = "#{@archive1_path}/lib/archive2.jar"
    @archive2_file = org.jboss.vfs::VFS.child( @archive2_path )
    @archive2_mount_point = org.jboss.vfs::VFS.child( @archive2_path )
    @archive2_handle = org.jboss.vfs::VFS.mountZip( @archive2_file, @archive2_mount_point, @temp_file_provider )


  end

  after(:each) do
    @archive2_handle.close
    @archive1_handle.close
  end

  describe "with vfs urls" do
    it "should allow globbing within archives with explicit vfs" do
      pattern = "vfs:#{@archive1_path}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include File.join( "vfs:#{@archive1_path}", 'web.xml' )
      items.should include File.join( "vfs:#{@archive1_path}", 'lib' )
    end
 
    it "should allow globbing within nested archives with explicit vfs" do
      pattern = "vfs:#{@archive2_path}/*"
      items = Dir.glob( pattern )
      items.should_not be_empty
      items.should include "vfs:#{@archive2_path}/manifest.txt"
    end
  end
  
  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      case ( style )
        when :relative
          prefix = "./#{TEST_DATA_BASE}"
        when :absolute
          prefix = File.expand_path( File.join( File.dirname( __FILE__ ), '..', TEST_DATA_BASE ) )
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

      it "should allow alternation globbing wiht trailing comma" do
        items = Dir.glob( "#{prefix}/home/todd/index{.en,}{.html,}{.erb,.haml,}" )
        items.should_not be_empty
        items.size.should eql 4
        items.should     include( "#{prefix}/home/todd/index.html.erb" )
        items.should     include( "#{prefix}/home/todd/index.en.html.erb" )
        items.should     include( "#{prefix}/home/todd/index.html.haml" )
        items.should     include( "#{prefix}/home/todd/index.en.html.haml" )
      end

      it "should allow for double-star globbing within archives" do
        #items = Dir.glob( "#{prefix}#{TEST_DATA_DIR}/home/larry/**/*{.zip,.jar,.ear}" )
        items = Dir.glob( "#{prefix}/home/larry/archive1.jar/**/*.jar" )
        items.should_not be_empty
        #items.size.should eql 1
        items.should     include( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar" )
        items.should     include( "#{prefix}/home/larry/archive1.jar/other_lib/subdir/archive6.jar" )
        items.should_not include( "#{prefix}/home/larry/archive1.jar/lib/archive4.txt" )
      end

    end
  end

end
