require 'spec_helper'

remote_describe 'jdk apis' do

  it "should be exposed" do
    lambda {
      javax.ejb.SessionBean
      javax.jms.Destination
      javax.mail.Address
      javax.servlet.Filter
      javax.transaction.Transaction
      javax.xml.stream.StreamFilter
    }.should_not raise_error
  end
end
