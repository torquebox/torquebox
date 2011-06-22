module OptionsMacros
  def it_should_allow_valid_options(&block)
    it "should allow valid options" do
      lambda {
        TorqueBox.configure &block
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

  def it_should_not_allow_invalid_options(&block)
    it "should not allow invalid options" do
      lambda {
        TorqueBox.configure &block
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

  def it_should_allow_valid_option_values(&block)
    it "should allow valid option values" do
      lambda {
        TorqueBox.configure &block
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

  def it_should_not_allow_invalid_option_values(&block)
    it "should not allow valid option values" do
      lambda {
        TorqueBox.configure &block
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

end

RSpec.configure do |config|
  config.extend(OptionsMacros)
end

