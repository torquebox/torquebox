require 'torquebox/messaging/processor_middleware/default_middleware'

class Thing
  include TorqueBox::Messaging::ProcessorMiddleware::DefaultMiddleware
end


describe TorqueBox::Messaging::ProcessorMiddleware::DefaultMiddleware do

  before(:each) do
    @thing = Thing.new
  end
  
  describe "#middleware" do
    it "should return the default middleware" do
      @thing.middleware.inspect.should == "[TorqueBox::Messaging::ProcessorMiddleware::WithTransaction]"
    end
  end
  

end
