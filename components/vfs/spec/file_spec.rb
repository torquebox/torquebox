
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "File extensions for VFS" do

  before(:each) do
    puts "mount"
    @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
    @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )

    @archive1_path = File.join( TEST_DATA_DIR, "/home/larry/archive1.jar" )
    @archive1_file = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_mount_point = org.jboss.vfs::VFS.child( @archive1_path )
    @archive1_handle = org.jboss.vfs::VFS.mountZip( @archive1_file, @archive1_mount_point, @temp_file_provider )

    @archive2_path = "#{@archive1_path}/lib/archive2.jar"
    @archive2_file = org.jboss.vfs::VFS.child( @archive2_path )
    @archive2_mount_point = org.jboss.vfs::VFS.child( @archive2_path )
    @archive2_handle = org.jboss.vfs::VFS.mountZip( @archive2_file, @archive2_mount_point, @temp_file_provider )


  end

  after(:each) do
    puts "unmount"
    @archive2_handle.close
    @archive1_handle.close
  end

  [ :absolute, :relative ].each do |style|
    describe "with #{style} paths" do

      case ( style )
        when :relative
          prefix = "./#{TEST_DATA_BASE}"
        when :absolute
          prefix = File.expand_path( File.join( File.dirname( __FILE__ ), '..', TEST_DATA_BASE ) )
      end

      it "should provide mtime for normal files" do
        mtime = File.mtime( "#{prefix}/home/larry/file1.txt" )
        mtime.should_not be_nil
      end

      it "should allow stat for normal files" do
        file = "#{prefix}/home/larry/file1.txt" 
        stat = File.stat( file )
        stat.should_not be_nil
        stat.mtime.should eql( File.mtime( file ) )
      end

      it "should provide mtime for files in an archive" do
        mtime = File.mtime( "#{prefix}/home/larry/archive1.jar/web.xml" )
        mtime.should_not be_nil
      end

      it "should allow stat for files in an archive" do
        file = "#{prefix}/home/larry/archive1.jar/web.xml"
        stat = File.stat( file )
        stat.should_not be_nil
        stat.mtime.should eql( File.mtime( file ) )
      end
    
      it "should provide mtime for files in a nested archive" do
        mtime = File.mtime( "#{prefix}/home/larry/archive1.jar/lib/archive2.jar/manifest.txt" )
        mtime.should_not be_nil
      end
    
      it "should test existance of normal files" do
        File.exist?( "#{prefix}/home/larry/file1.txt" ).should be_true
        File.exist?( "#{prefix}/home/larry/file42.txt" ).should be_false
      end
    
      it "should test existance of files in an archive" do
        File.exist?( "#{prefix}/home/larry/archive1.jar/web.xml" ).should be_true
      end
    
      it "should test directoryness for normal files" do
        File.directory?( "#{prefix}/home/larry" ).should be_true
        File.directory?( "#{prefix}/home/larry/file1.txt" ).should be_false
      end

      it "should test directoryness for files within an archive" do
        File.directory?( "#{prefix}/home/larry/archive1.jar/lib" ).should be_true
        File.directory?( "#{prefix}/home/larry/archive1.jar/web.xml" ).should be_false
      end

      it "should test fileness for normal files" do
        File.file?( "#{prefix}/home/larry" ).should be_false
        File.file?( "#{prefix}/home/larry/file1.txt" ).should be_true
      end

      it "should test fileness for files within an archive" do
        File.file?( "#{prefix}/home/larry/archive1.jar/lib" ).should be_false
        File.file?( "#{prefix}/home/larry/archive1.jar/web.xml" ).should be_true
      end
    end
  end
end
