
module VFS
  module Ext
    class VirtualFile < SimpleDelegator

      def initialize(io, path=nil)
        super(io)
        @path = path
      end

      def atime()
        ::File.atime( path )
      end

      def chmod(mode_int)
        ::File.chmod( mode_int, path )
      end

      def chown(owner_int, group_int)
        ::File.chown( owner_int, group_int, path )
      end

      def ctime()
        ::File.ctime( path )
      end

      def flock(locking_constant)
        # not supported
      end

      def lstat()
        ::File.stat( path )
      end

      def mtime()
        ::File.mtime( path )
      end

      def o_chmod(mode_int)
        self.chmod(mode_int)
      end

      def path()
        @path
      end

      def truncate(max_len)
      end

    end
  end
end
