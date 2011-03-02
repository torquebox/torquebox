require 'active_support/cache/torque_box_store'

describe ActiveSupport::Cache::TorqueBoxStore do

  describe "basics" do
    before(:each) do
      puts "JC: before"
    end

    after(:each) do
      puts "JC: after"
    end

    it "should behave" do
      puts "JC: during"
    end

  end

end
