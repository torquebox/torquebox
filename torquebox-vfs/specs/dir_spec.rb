
require File.dirname(__FILE__) +  '/spec_helper.rb'

describe "Dir extensions for VFS" do

  it "should allow appropriate globbing of normal files" do
    puts "stdout.HI"
    stderr.puts "stderr.HI"
    
    items = Dir.glob( "#{TEST_DATA_DIR}/home/larry/*" )
    items.should_not be_empty
  end

end
