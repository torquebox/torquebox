require 'spec_helper'

describe "vhost rack test" do

  deploy :name=>'vhosting-test', :paths=> [ 
    "rack/vhost-app1-knob.yml",
    "rack/vhost-app2-knob.yml",
  ]

  before(:each) do
    @original_capy_app_host = Capybara.app_host
  end

  after(:each) do
    Capybara.app_host = @original_capy_app_host
  end

  it "should work" do
    Capybara.app_host = "http://integ-app1.torquebox.org:8080"
    visit "/vhosting"
    page.should have_content('it worked')
    page.should have_selector( ".this-is-app-one" )
    page.should_not have_selector( ".this-is-app-two" )

    Capybara.app_host = "http://integ-app2.torquebox.org:8080"
    visit "/vhosting"
    page.should have_content('it worked')
    page.should have_selector( ".this-is-app-two" )
    page.should_not have_selector( ".this-is-app-one" )
  end

end
