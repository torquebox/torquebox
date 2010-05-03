
require 'torquebox/messaging/metadata/builder'

describe TorqueBox::Messaging::MetaData::Builder do

  before :each do
    @builder = TorqueBox::Messaging::MetaData::Builder.new
  end

  it "must produce empty metadata if un-used" do
    @builder.should_not be_nil 
  end

end

