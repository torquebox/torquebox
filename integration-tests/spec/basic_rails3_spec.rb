require 'spec_helper'

describe "basic rack test" do

  deploy "rails3/basic-knob.yml"

  it "should work" do
    visit "/basic-rails"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'basic-rails'
  end

end
