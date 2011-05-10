require 'java'

$: << File.dirname(__FILE__) + '/../lib' 

require 'torquebox/vfs'

TEST_DATA_BASE = 'target/test-data'
TEST_COPY_BASE = File.join( File.dirname( TEST_DATA_BASE ), 'test-copy' )

TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

module PathHelper
  def self.extended(cls)
    cls.class_eval do

      def pwd()
        self.class.pwd
      end

      def self.pwd()
        ::Dir.pwd
      end

      def archive1_vfs_path()
        vfs_path( @archive1_path )
      end
 
      def archive2_vfs_path()
        vfs_path( @archive2_path )
      end

      def archive1_path()
        @archive1_path
      end

      def archive2_path()
        @archive2_path
      end

      def absolute_prefix
        self.class.absolute_prefix
      end

      def self.absolute_prefix
        return '' unless ( TESTING_ON_WINDOWS )
        'C:'
      end
  
      def vfs_path(path)
        self.class.vfs_path( path )
      end

      def self.vfs_path(path)
        return path                   if ( path[0,4] == 'vfs:' )
        return "vfs:#{path}"          if ( path[0,1] == '/' )
        return "vfs:#{path}"          if ( path[0,1] == '\\' )
        return vfs_path( "/#{path}" ) if ( path =~ %r(^[a-zA-Z]:) )
        return vfs_path( File.join( pwd, path ) )
      end

      def test_data_base_path(style)
        self.class.test_data_base_path(style)
      end

      def self.test_data_base_path(style)
        path = nil
        case ( style )
          when :relative
            path = "./#{TEST_DATA_BASE}"
          when :absolute
            path = File.expand_path( File.join( File.dirname( __FILE__ ), '..', TEST_DATA_BASE ) )
          when :vfs
            path = vfs_path( test_data_base_path( :absolute ) )
        end
  if ( path =~ /^[a-z]:/ )
    path = path[0,1].upcase + path[1..-1]
  end
        path
      end

      def test_copy_base_path(style)
        self.class.test_copy_base_path(style)
      end

      def self.test_copy_base_path(style)
        path = nil
        case ( style )
          when :relative
            path = "./#{TEST_COPY_BASE}"
          when :absolute
            path = File.expand_path( File.join( File.dirname( __FILE__ ), '..', TEST_COPY_BASE ) )
          when :vfs
            path = vfs_path( test_copy_base_path( :absolute ) )
        end
  if ( path =~ /^[a-z]:/ )
    path = path[0,1].upcase + path[1..-1]
  end
        path
      end


    end

  end
end

module TestDataHelper

  def self.extended(cls)

    cls.extend( PathHelper )

    cls.before(:each) do
      @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
      @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )
  
      @archive1_path        = File.expand_path( "#{test_data_base_path(:absolute)}/home/larry/archive1.jar" )
      @archive1_file        = org.jboss.vfs::VFS.child( @archive1_path )
      @archive1_mount_point = org.jboss.vfs::VFS.child( @archive1_path )
      @archive1_handle      = org.jboss.vfs::VFS.mountZip( @archive1_file, @archive1_mount_point, @temp_file_provider )
  
      @archive2_path        = "#{@archive1_path}/lib/archive2.jar"
      @archive2_file        = org.jboss.vfs::VFS.child( @archive2_path )
      @archive2_mount_point = org.jboss.vfs::VFS.child( @archive2_path )
      @archive2_handle      = org.jboss.vfs::VFS.mountZip( @archive2_file, @archive2_mount_point, @temp_file_provider )
    end

    cls.after(:each) do
      @archive2_handle.close
      @archive1_handle.close
      @temp_file_provider.close
      @executor.shutdown
    end

  end
end

module TestDataCopyHelper

  def self.extended(cls)

    cls.extend( PathHelper )

    cls.before(:each) do
      FileUtils.rm_rf( test_copy_base_path(:absolute) )
      FileUtils.cp_r( test_data_base_path(:absolute), test_copy_base_path(:absolute) )

      @executor = java.util.concurrent::Executors.newScheduledThreadPool( 1 )
      @temp_file_provider = org.jboss.vfs::TempFileProvider.create( "vfs-test", @executor )
  
      @archive1_path = File.join( test_copy_base_path(:absolute), "/home/larry/archive1.jar" )
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
      @temp_file_provider.close
      @executor.shutdown
      FileUtils.rm_rf( test_copy_base_path(:absolute) )
    end

  end
end
