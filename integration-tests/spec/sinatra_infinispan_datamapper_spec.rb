require 'spec_helper'

describe "sinatra with dm-infinispan-adapter" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/datamapper
      env: development
    web:
      context: /sinatra-datamapper
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work" do
    visit "/sinatra-datamapper"
    page.should have_content('It Works!')
  end

  it "should list widgets" do
    visit "/sinatra-datamapper/muppets"
    page.should have_content('Muppet Count: 3')
    page.should have_content('Muppet 1: Big Bird')
    page.should have_content('Muppet 2: Snuffleupagus')
    page.should have_content('Muppet 3: Cookie Monster')
  end

end

