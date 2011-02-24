
require 'org/torquebox/auth/authenticator'

describe TorqueBox::Authentication do
  it "should provide a default authenticator"
  it "should allow authenticators to be looked up by name"
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

  before :each do
    @auth_bean = mock(Object)
    @auth_bean.stub! :authenticate
    @auth = TorqueBox::Authenticator.new(@auth_bean)
  end

end
