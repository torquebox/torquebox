require 'spec_helper'

remote_describe 'jdk apis' do

  it "should be exposed" do
    lambda {
      javax.jms.Destination
      javax.transaction.Transaction
    }.should_not raise_error
  end
end
