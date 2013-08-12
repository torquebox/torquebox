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
  sun_jdk = org.jruby.util.unsafe.UnsafeGetter.getUnsafe rescue nil
  if sun_jdk
    it "should have sun.jdk modules available" do
      lambda {
        org.jruby.util.unsafe.UnsafeGetter.getUnsafe
      }.should_not raise_error
    end
  end
end
