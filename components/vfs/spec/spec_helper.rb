require 'java'

$: << File.dirname(__FILE__) + '/../lib' 

require 'vfs'

TEST_DATA_BASE = 'target/test-data'
TEST_DATA_DIR = File.expand_path( File.join( File.dirname(__FILE__), "/../#{TEST_DATA_BASE}" ) )
TEST_COPY_BASE = File.join( File.dirname( TEST_DATA_BASE ), 'test-copy' )

module TestDataHelper

  def self.extended(cls)
    puts "extended by #{cls}"

    cls.before(:each) do
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

    cls.after(:each) do
      @archive2_handle.close
      @archive1_handle.close
    end
  end
end

module TestDataCopyHelper

  def self.extended(cls)
    puts "extended by #{cls}"

    cls.before(:each) do

      test_copy_dir = File.join( File.dirname( TEST_DATA_DIR ), 'test-copy' )
      FileUtils.rm_rf( test_copy_dir )
      FileUtils.cp_r( TEST_DATA_DIR, test_copy_dir )
      @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
      @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )
  
      #@archive1_path = File.join( TEST_DATA_DIR, "/home/larry/archive1.jar" )
      @archive1_path = File.join( test_copy_dir, "/home/larry/archive1.jar" )
      @archive1_file = org.jboss.vfs::VFS.child( @archive1_path )
      @archive1_mount_point = org.jboss.vfs::VFS.child( @archive1_path )
      @archive1_handle = org.jboss.vfs::VFS.mountZip( @archive1_file, @archive1_mount_point, @temp_file_provider )
  
      @archive2_path = "#{@archive1_path}/lib/archive2.jar"
      @archive2_file = org.jboss.vfs::VFS.child( @archive2_path )
      @archive2_mount_point = org.jboss.vfs::VFS.child( @archive2_path )
      @archive2_handle = org.jboss.vfs::VFS.mountZip( @archive2_file, @archive2_mount_point, @temp_file_provider )
    end

    cls.after(:each) do
      @archive2_handle.close
      @archive1_handle.close
    end
  end
end
