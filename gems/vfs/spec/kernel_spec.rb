
require File.dirname(__FILE__) + '/spec_helper.rb'

describe "Kernel stuff" do

  extend PathHelper
  extend TestDataCopyHelper

  # 
  # Kernel.require_relative is a 1.9ism
  #

  if ( Kernel.respond_to? :require_relative )
    it "should be able to handle require_relative" do
      require_relative "relatively_requireable"
    end
  
    it "should be able to handle require_relative with VFS __FILE__" do
      eval( 'require_relative "relatively_requireable"', binding, vfs_path(__FILE__) )
    end
  end

  unless TESTING_ON_WINDOWS
    it "should remove vfs: prefix from backtick commands" do
      prefix = test_copy_base_path( :vfs )
      path = File.join( prefix, "home", "larry", "file1.txt" )
      result = `ls #{path} 2>&1`
      result.should_not match( /No such file/i )
    end
  end

end
