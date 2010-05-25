
require 'torquebox/container/foundation'
require 'torquebox/container/naming_enabler'

describe TorqueBox::Container::NamingEnabler do

  describe 'basics' do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new

      @container.enable( TorqueBox::Container::NamingEnabler ) do
      end

      @container.start
    end
    after(:each) do
      @container.stop
    end

    it "should have a Naming bean" do
      naming_bean = @container['Naming']
      naming_bean.should_not be_nil
    end
  end

end
