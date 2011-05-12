require 'spec_helper'

asset_root = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: development
    web:
      context: /
      host: integ-app1.torquebox.org
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

asset_context = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: development
    web:
      context: /basic-rails2-asset
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

describe "basic rails2 asset test" do

  deploy asset_root, asset_context

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
