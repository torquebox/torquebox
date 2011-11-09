require File.dirname(__FILE__) + '/spec_helper.rb'

describe "Process compatibility with VFS" do

  extend TestDataCopyHelper

  if Process.respond_to?(:spawn) && !TESTING_ON_WINDOWS
    it "should remove vfs: prefix from spawned commands" do
      prefix = test_copy_base_path( :vfs )
      path = File.join( prefix, "home", "larry", "file1.txt" )
      r, w = IO.pipe
      Process.spawn( {}, "ls #{path} 2>&1", STDERR => w, STDOUT => w )
      w.close
      result = r.readlines.join("")
      r.close
      result.should_not match( /No such file/i )
    end
  end

end
