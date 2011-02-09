require 'spec_helper'

describe "vhost rack test" do

  deploy :path => "rack/vhost-knob.yml"

  it "should work" do
    Capybara.app_host = "http://integ-app1.torquebox.org:8080"
    visit "/vhosting"
    page.should have_content('it worked')
  end

end
