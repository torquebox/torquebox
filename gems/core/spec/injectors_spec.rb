
require 'torquebox/injectors'

describe TorqueBox::Injectors do

  include TorqueBox::Injectors

  it 'should return the same thing for all fetch types' do
    TorqueBox::Registry.merge!('this' => :that)
    TorqueBox::Registry['this'].should == :that
    fetch('this').should == :that
    fetch_msc('this').should == :that
    fetch_service('this').should == :that
    fetch_cdi(:this).should == :that
    fetch_jndi('this').should == :that
    fetch_queue('this').should == :that
    fetch_topic('this').should == :that
  end

end

describe 'TorqueBox::Injectors without include' do
  it 'should work' do
    TorqueBox::Registry.merge!('this' => :that)
    TorqueBox::Registry['this'].should == :that
    TorqueBox.fetch('this').should == :that
  end
end
