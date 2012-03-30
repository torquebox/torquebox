
require 'torquebox/injectors'

describe TorqueBox::Injectors do

  include TorqueBox::Injectors

  it "should return the same thing for all injection types" do
    TorqueBox::Registry.merge!('this' => :that)
    TorqueBox::Registry['this'].should == :that
    inject('this').should == :that
    inject_msc('this').should == :that
    inject_service('this').should == :that
    inject_cdi(:this).should == :that
    inject_jndi('this').should == :that
    inject_queue('this').should == :that
    inject_topic('this').should == :that
  end

end

