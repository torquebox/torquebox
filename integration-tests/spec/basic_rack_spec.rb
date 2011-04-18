require 'spec_helper'

describe "basic rack test" do

  deploy "rack/basic-knob.yml"

  it "should work" do
    visit "/basic-rack"
    page.should have_content('it worked')
    page.find("#success")[:class].should == 'basic-rack'
  end

end
