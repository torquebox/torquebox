require File.dirname(__FILE__) + '/spec_helper.rb'

describe "File extensions for VFS" do

  it "should delegate to File.directory?" do
    delegate_to_file(:directory?, 'file')
  end

  it "should delegate to File.exist?" do
    delegate_to_file(:exist?, 'file')
  end

  it "should delegate to File.exists?" do
    delegate_to_file(:exists?, 'file')
  end

  it "should delegate to File.file?" do
    delegate_to_file(:file?, 'file')
  end

  it "should delegate to File.readable?" do
    delegate_to_file(:readable?, 'file')
  end

  it "should delegate to File.writable?" do
    delegate_to_file(:writable?, 'file')
  end

  def delegate_to_file(*args)
    method = args.shift
    File.should_receive(method).with(*args).and_return('value')
    FileTest.send(method, *args).should == 'value'
  end
end
