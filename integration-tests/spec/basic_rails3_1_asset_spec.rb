require 'spec_helper'

describe "basic rails3.1 asset test" do

  deploy <<-END.gsub(/^ {4}/, '')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/basic
      RAILS_ENV: development
    web:
      context: /basic-rails31-asset

    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should serve assets from app/assets" do
    visit "/basic-rails31-asset/assets/test.js"
    page.source.should =~ /\/\/ taco/
  end

  it "should generate correct asset and link paths" do
    visit "/basic-rails31-asset"
    image = page.find('img')
    image['src'].should match(/\/basic-rails31-asset\/assets\/rails\.png/)
    link = page.find('a')
    link['href'].should eql('/basic-rails31-asset/')
  end
end
