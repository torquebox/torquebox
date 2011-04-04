
require File.dirname(__FILE__) + '/spec_helper.rb'
require 'tempfile'
require 'pathname'

describe "File extensions for VFS" do

  extend TestDataCopyHelper

  it "should report writable-ness for VFS urls" do
    prefix = test_copy_base_path( :relative )
    url = vfs_path( "#{prefix}/home/larry/file1.txt" )
    File.exists?( url ).should be_true
    File.exist?( url ).should be_true
    File.writable?( url ).should be_true
  end
  
  it "should not loop infinitely" do
    File.exists?("/nothingtoseehere").should be_false
    File.exists?(vfs_path("/nothingtoseehere")).should be_false
  end

  describe "expand_path" do
    it "should handle relative non-vfs path" do
      File.expand_path("../foo", "/tmp/bar").should == "#{absolute_prefix}/tmp/foo"
    end

    it "should handle relative to vfs path" do
      File.expand_path("../foo", vfs_path("/tmp/bar") ).should == vfs_path("#{absolute_prefix}/tmp/foo")
    end

    it "should expand paths relative to VFS urls as VFS" do
      absolute = File.expand_path("db/development.sqlite3", vfs_path("/path/to/app") )
      absolute.should eql( vfs_path("#{absolute_prefix}/path/to/app/db/development.sqlite3") )
    end

    it "should expand paths relative to VFS pathnames as VFS" do
      absolute = File.expand_path("db/development.sqlite3", Pathname.new( vfs_path( "/path/to/app" ) ) )
      absolute.should eql( vfs_path("#{absolute_prefix}/path/to/app/db/development.sqlite3") )
    end

    it "should expand absolute Pathname objects correctly" do
      File.expand_path( vfs_path("/foo") ).should eql( vfs_path("/foo") )
      File.expand_path(Pathname.new( vfs_path("/foo"))).should eql( vfs_path("/foo") )
    end

    it "should return first path when given two vfs paths" do
      File.expand_path( vfs_path("/tmp/foo"), vfs_path("/tmp/bar")).should == vfs_path("/tmp/foo")
    end
  end

  it "should handle vfs urls as readable" do
    File.readable?( __FILE__ ).should be_true
    File.readable?( vfs_path(__FILE__) ).should be_true
  end

  it "should report readable-ness for files inside vfs archives" do
    path = "#{archive1_vfs_path}/web.xml"
    File.readable?( path ).should be_true
  end

  it "should report readable-ness for non-existent files inside vfs archives" do
    path = "#{archive1_vfs_path}/file_that_does_not_exist.txt"
    File.readable?( path ).should be_false
  end

  it "should handle #'s in filenames properly" do
    prefix = test_copy_base_path( :absolute )
    File.file?( "#{prefix}/#bad-uri#" ).should be_true
    File.file?( vfs_path( "#{prefix}/#bad-uri#" ) ).should be_true
    File.file?( vfs_path( "#{prefix}/#missing#" ) ).should be_false
  end

  it "should handle spaces in filenames properly" do
    prefix = test_copy_base_path( :absolute )
    File.file?( "#{prefix}/sound of music/flibbity jibbit" ).should be_true
    File.file?( vfs_path("#{prefix}/sound of music/flibbity jibbit" ) ).should be_true
    File.file?( vfs_path("#{prefix}/sound of music/flibberty gibbet" ) ).should be_false
  end

  it "should handle percent-encoded filenames" do
    prefix = test_copy_base_path( :absolute )
    File.file?( "#{prefix}/views%2Flocalhost%3A8080%2Fposts" ).should be_true
    File.file?( vfs_path("#{prefix}/views%2Flocalhost%3A8080%2Fposts" ) ).should be_true
    File.file?( vfs_path("#{prefix}/views%2Flocalhost%3A8080%2Fmissing" ) ).should be_false
  end

  it "should handle backslashes in filenames even though there's no good reason to use them regardless of platform" do
    filename = __FILE__.gsub("/","\\")
    File.readable?( filename ).should be_true
    File.readable?( vfs_path( filename ) ).should be_true
  end

  it "should be able to chmod real files with vfs urls" do
    path = File.expand_path("foo")
    begin
      f = File.new(path, "w")
      FileUtils.chmod( 0666, vfs_path( path ) )
      m1 = f.stat.mode
      FileUtils.chmod( 0644, vfs_path( path ) )
      m2 = f.stat.mode
      m1.should_not eql(m2) unless TESTING_ON_WINDOWS
    ensure
      File.delete(path) rescue nil
    end
  end

  it "should be able to read file after chmod from a stat" do
    # Similar to what Rails' File.atomic_write does (TORQUE-174)
    p1 = vfs_path( File.expand_path("p1") )
    p2 = vfs_path( File.expand_path("p2") )
    begin
      File.open(p1, "w") { }
      File.open(p2, "w") { }
      File.chmod(File.stat(p1).mode, p2)
      File.read(p2)
    ensure
      File.unlink(p1, p2) rescue nil
    end
  end

  it "should chmod inside vfs archive when directory mounted on filesystem" do
    FileUtils.rm_rf "target/mnt"
    archive = org.jboss.vfs::VFS.child( @archive1_path )
    logical = archive.getChild( "lib" )
    physical = java.io::File.new( "target/mnt" )
    physical.mkdirs
    mount = org.jboss.vfs::VFS.mountReal( physical, logical )
    path = "#{@archive1_path}/lib/chmod_test"
    begin
      lambda {
        f = File.new("target/mnt/chmod_test", "w" )
        FileUtils.chmod( 0666, path )
        m1 = f.stat.mode
        FileUtils.chmod( 0755, path )
        m2 = f.stat.mode
        m1.should_not eql(m2) unless TESTING_ON_WINDOWS
      }.should_not raise_error
    ensure
      mount.close
    end
  end

  it "should be able to create new files with vfs urls" do
    lambda {
      File.new( vfs_path( __FILE__ ), 'r')
    }.should_not raise_error
  end

  it "should allow rm_rf and mkdir_p of vfs path" do
    parent = Dir.tmpdir
    workdir = parent + '/vfs-test'
    child = workdir + "/b/c"
    FileUtils.rm_rf vfs_path( workdir )
    File.exist?( vfs_path( child ) ).should be_false
    FileUtils.mkdir_p( vfs_path( child ) )
    File.exist?( vfs_path( child ) ).should be_true
    FileUtils.rm_rf vfs_path( workdir )
    File.exist?( vfs_path( workdir ) ).should be_false
    File.exist?( vfs_path( parent ) ).should be_true
  end

  describe "Tempfiles" do
    it "should be created in default dir" do
      lambda {
        t = Tempfile.new("temp_file_test")
        File.exist?(t.path).should be_true
      }.should_not raise_error
    end

    it "should be created in a non-vfs directory" do
      lambda {
        t = Tempfile.new("temp_file_test", Dir.tmpdir)
        File.exist?(t.path).should be_true
      }.should_not raise_error
    end

    it "should be created in a vfs directory" do
      lambda {
        t = Tempfile.new("temp_file_test", "vfs:#{Dir.tmpdir}")
        File.exist?(t.path).should be_true
      }.should_not raise_error
    end
  end

  describe "open" do
    it "should return File when called on File with VFS url" do
      f = File.open( archive1_vfs_path, 'r')
      f.should be_an_instance_of(File)
      f.close
    end

    it "should return File when called on File without VFS url" do
      f = File.open(archive1_path, 'r')
      f.should be_an_instance_of(File)
      f.close
    end

    it "should find files by pathnames" do
      lambda {
        f = File.open(Pathname.new(archive1_path), 'r')
	f.close
      }.should_not raise_error
    end
  end

  describe "new" do
    it "should return File when called on File with VFS url" do
      f = File.new( archive1_vfs_path, 'r')
      f.should be_an_instance_of(File)
      f.close
    end

    it "should return File when called on File without VFS url" do
      f = File.new( archive1_path, 'r')
      f.should be_an_instance_of(File)
      f.close
    end

    it "should create objects that respond to lstat for files in an archive" do
      f = File.new( "#{archive1_vfs_path}/web.xml")
      f.lstat.should_not be_nil
      f.close
    end
  end

  [ :absolute, :relative, :vfs ].each do |style|
    describe "with #{style} paths" do
      case ( style )
        when :relative
          prefix = test_copy_base_path( :relative )
        when :absolute
          prefix = test_copy_base_path( :absolute )
        when :vfs
          prefix = test_copy_base_path( :vfs )
      end

      unless TESTING_ON_WINDOWS && ( style == :absolute ) # WTF?
        it "should provide size for normal files" do
          path = "#{prefix}/home/larry/file1.txt"
          s = File.size( path )
          s.should_not be_nil
          s.should be > 0
        end
      end

      it "should throw NOENT for size of non-existant files" do
        lambda {
          File.size( "#{prefix}/home/larry/NOT_REALLY_file1.txt" )
        }.should raise_error
      end

  
      unless TESTING_ON_WINDOWS && ( style == :absolute ) # WTF?
        it "should provide size? for normal files" do
          s = File.size?( "#{prefix}/home/larry/file1.txt" )
          s.should_not be_nil
          s.should be > 0
        end
      end

      it "should not throw NOENT for size? of non-existant files" do
        lambda {
          s = File.size?( "#{prefix}/home/larry/NOT_REALLY_file1.txt" )
          s.should be_nil
        }.should_not raise_error
      end

      it "should provide mtime for normal files" do
        mtime = File.mtime( "#{prefix}/home/larry/file1.txt" )
        mtime.should_not be_nil
      end

      it "should report writeable-ness for normal files" do
        File.writable?( "#{prefix}/home/larry/file1.txt" ).should be_true
      end

       # move to kernel_spec
      it "should allow writing with truncation via open()" do
        open( "#{prefix}/home/larry/file1.txt", (File::WRONLY | File::TRUNC | File::CREAT) ) do |file|
          file.puts "howdy"
        end
        contents = File.read( "#{prefix}/home/larry/file1.txt" )
        contents.should eql( "howdy\n" )
      end

       # move to kernel_spec
      it "should allow writing with appending via open()" do
        open( "#{prefix}/home/larry/file1.txt", (File::WRONLY | File::APPEND | File::CREAT) ) do |file|
          file.puts "howdy"
        end
        contents = File.read( "#{prefix}/home/larry/file1.txt" )
        contents.should eql( "This is file 1\nhowdy\n" )

        fs_file = File.join( "#{test_copy_base_path(:relative)}/home/larry/file1.txt" )
        fs_contents = File.read( fs_file )
        fs_contents.should eql( "This is file 1\nhowdy\n" )
      end

      it "should allow writing new files via File.open" do
        File.open( "#{prefix}/home/larry/new_file.txt", 'w' ) do |file|
          file.puts "howdy"
        end
        contents = File.read( "#{prefix}/home/larry/new_file.txt" )
        contents.should eql( "howdy\n")
      end

      it "should allow stat for normal files" do
        file = "#{prefix}/home/larry/file1.txt"
        stat = File.stat( file )
        stat.should_not be_nil
        stat.mtime.to_s.should eql( File.mtime( file ).to_s )
      end

      it "should not return a stat for missing files" do
        lambda {
          stat = File.stat( "missing file" )
        }.should raise_error(Errno::ENOENT)
        lambda {
          stat = File.stat( vfs_path("/missing/file") )
        }.should raise_error(Errno::ENOENT)
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

      it "should test directoryness for non-existant files" do
        File.directory?( "#{prefix}/home/larry/archive1.jar/fib" ).should be_false
        File.directory?( "#{prefix}/home/larry/archive1.jar/tacos" ).should be_false
        File.directory?( "#{prefix}/tacos" ).should be_false
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

  describe 'path joinage' do
    it "should work for relative paths" do
      File.join("",       "./foo").should == "/./foo"
      File.join("/",      "./foo").should == "/./foo"
      File.join("vfs:",   "./foo").should == "vfs:/./foo"
      File.join("vfs:/",  "./foo").should == "vfs:/./foo"
    end
    it "should work for absolute paths" do
      File.join("",       "/foo").should == "/foo"
      File.join("/",      "/foo").should == "/foo"
      File.join("vfs:",   "/foo").should == "vfs:/foo"
      File.join("vfs:/",  "/foo").should == "vfs:/foo"
    end
    it "should work for vfs paths" do
      File.join("",       "vfs:/foo").should == "/foo"
      File.join("/",      "vfs:/foo").should == "/foo"
      File.join("vfs:",   "vfs:/foo").should == "vfs:/foo"
      File.join("vfs:/",  "vfs:/foo").should == "vfs:/foo"
    end
    it "should handle a subset of vfs params" do
      File.join('a', 'b', 'c', 'vfs:/d', 'e').should      == 'a/b/c/d/e'
      File.join('vfs:a', 'b', 'c', 'vfs:/d', 'e').should  == 'vfs:a/b/c/d/e'
      File.join('vfs:/a', 'b', 'c', 'vfs:/d', 'e').should == 'vfs:/a/b/c/d/e'
    end
  end

  describe 'dirname' do
    it "should properly handle non-vfs paths" do
      File.dirname('/').should == '/'
      File.dirname('/a').should == '/'
      File.dirname('/a/b').should == '/a'
    end

    it "should return vfs paths when given a vfs path" do
      File.dirname('vfs:/').should == 'vfs:/'
      File.dirname('vfs:/a').should == 'vfs:/'
      File.dirname('vfs:/a/b').should == 'vfs:/a'
    end
  end
  
  describe 'chown' do
    it "should handle vfs paths" do
      path = archive1_vfs_path
      stat = File.stat(path)
      File.chown( stat.uid, stat.gid, path )
    end
  end

  describe 'utime' do
    it "should handle vfs paths" do
      path = File.expand_path("foo")
      begin
        File.new(path, "w")
        vpath = vfs_path( path )
        mtime = File.mtime(vpath)
        File.utime( Time.now, mtime+1, vpath )
        mtime.should be < File.mtime(vpath)
      ensure
        File.delete(path) rescue nil
      end
    end
  end

  describe 'subclasses' do
    it "should open an instance of the subclass" do
      class MyFile < File
        def new_method; 'hello'; end
      end
      MyFile.open(__FILE__).new_method.should == 'hello'
    end
  end

end
