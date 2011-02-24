
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
    obj = mock(Object)
    obj.should_receive :foo
    @auth.authenticate('scott', 'tiger') do
      obj.foo
    end
  end

  before :each do
    @auth_bean = mock(Object)
    @auth_bean.stub! :authenticate
    @auth = TorqueBox::Authenticator.new(@auth_bean)
  end

end
