require 'spec_helper'

remote_describe 'jdk apis' do

  it "should be exposed" do
    lambda {
      javax.jms.Destination
      javax.transaction.Transaction
    }.should_not raise_error
  end

  # Check on the client-side if we're using a Sun JDK
  # before running any of those tests server-side
  unless ENV['TORQUEBOX_INTEG_SUN_JDK']
    ENV['TORQUEBOX_INTEG_SUN_JDK'] = "#{com.sun.org.apache.xpath.internal.VariableStack}" rescue nil
  end
  if ENV['TORQUEBOX_INTEG_SUN_JDK']
    it "should have sun.jdk modules available" do
      lambda {
        com.sun.org.apache.xpath.internal.VariableStack
        com.sun.org.apache.xpath.internal.jaxp.JAXPExtensionsProvider
        com.sun.org.apache.xml.internal.utils.PrefixResolver
      }.should_not raise_error
    end
  end
end
