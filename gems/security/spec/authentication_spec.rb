require 'rubygems'
require 'torquebox/kernel'
require 'torquebox/security/authentication'

describe TorqueBox::Authentication do
 it "should provide a default authenticator" do
   TorqueBox::ServiceRegistry.should_receive(:lookup).with 'torquebox.authentication.myapp.default'
   TorqueBox::Authentication.default
 end

 it "should allow authenticators to be looked up by name" do
   TorqueBox::ServiceRegistry.should_receive(:lookup).with 'torquebox.authentication.myapp.namedthing'
   TorqueBox::Authentication['namedthing']
 end

 it "should return nil if TorqueBox is not running" do
   ENV['TORQUEBOX_APP_NAME'] = nil
   TorqueBox::Authentication.default.should be_nil
 end

 before :each do
   TorqueBox::Kernel.stub! :lookup
   ENV['TORQUEBOX_APP_NAME'] = 'myapp'
 end

end


describe TorqueBox::Authenticator do

 it "should delegate authentication requests" do
   @auth_bean.should_receive(:authenticate).with('scott', 'tiger')
   @auth.authenticate('scott', 'tiger')
 end

 it "should accept a block to scope actions to authentication" do
   obj = nil
   @auth_bean.should_receive(:authenticate).and_return(true)
   @auth.authenticate('scott', 'tiger') do
     obj = 'foo'
   end
   obj.should eql('foo')
 end

 it "should only call a provided block if the credentials authenticate" do
   obj = nil
   @auth_bean.should_receive(:authenticate).and_return(false)
   @auth.authenticate('scott', 'tiger') do
     obj = 'foo'
   end
   obj.should be_nil
 end

 it "should return false if delegated auth_bean is found" do
   @auth_bean = nil
   @auth = TorqueBox::Authenticator.new(@auth_bean)
   @auth.authenticate('scott', 'tiger').should be_false
 end

 before :each do
   @auth_bean = mock(Object)
   @auth_bean.stub! :authenticate
   @auth = TorqueBox::Authenticator.new(@auth_bean)
 end

end

