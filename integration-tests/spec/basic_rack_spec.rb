require 'spec_helper'

describe "basic rack test" do

  deploy :path => "rack/1.1.0/basic-knob.yml"

  it "should work" do
    visit "/basic-rack"
    page.should have_content('it worked')
  end

end
