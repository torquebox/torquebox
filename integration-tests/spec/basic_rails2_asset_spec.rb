require 'spec_helper'

describe "basic rails2 asset test" do

  deploy "rails2/basic-asset-root-knob.yml", "rails2/basic-asset-context-knob.yml"

  before(:each) do
    @original_capy_app_host = Capybara.app_host
  end

  after(:each) do
    Capybara.app_host = @original_capy_app_host
  end

  it "should work for rails2 at root context" do
    Capybara.app_host = "http://integ-app1.torquebox.org:8080"
    visit "/"
    image = page.find('img')
    image['src'].should match(/^\/images\/rails\.png/)
  end

  it "should work for rails2 at non-root context" do
    visit "/basic-rails2-asset"
    image = page.find('img')
    image['src'].should match(/^\/basic-rails2-asset\/images\/rails\.png/)
  end
end
