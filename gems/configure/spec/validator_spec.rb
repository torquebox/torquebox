require 'torquebox/configuration/validator'

include TorqueBox::Configuration

describe TorqueBox::Configuration::Validator do

  context "required options" do
    it "should be valid if all of the required options are there" do
      Validator.new({ :required => [:foo, :bar]}, 'name', {:foo => :x, :bar => :y}).should be_valid
    end

    it "should not be valid if not all of the required options are there" do
      Validator.new({ :required => [:foo, :bar]}, 'name', {:foo => :x}).should_not be_valid
    end
  end

  context "allowed options" do
    it "should be valid if no non-allowed options are there" do
      Validator.new({ :optional => [:foo, :bar]}, 'name', {:foo => :x}).should be_valid
    end

    it "should not be valid if any non-allowed options are there" do
      Validator.new({ :optional => [:foo, :bar]}, 'name', {:cheese => :x}).should_not be_valid
    end
  end

  context "allowed values" do
    it "should be valid if an allowed value is used" do
      Validator.new({ :optional => [{ :foo => ['gouda', 'havarti']}, :bar]}, 'name', {:foo => 'gouda'}).should be_valid
    end

    it "should be invalid if an non-allowed value is used" do
      Validator.new({ :optional => [{ :foo => ['gouda', 'havarti']}, :bar]}, 'name', {:foo => 'swiss'}).should_not be_valid
    end
  end
end
