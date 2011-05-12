require 'spec_helper'

app1 = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic
      env: development
    web:
      context: /vhosting
      host: integ-app1.torquebox.org
    environment:
      GRIST: this-is-app-one
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

app2 = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic
      env: development
    web:
      context: /vhosting
      host: integ-app2.torquebox.org
    environment:
      GRIST: this-is-app-two
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

describe "vhost rack test" do

  deploy app1, app2

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
