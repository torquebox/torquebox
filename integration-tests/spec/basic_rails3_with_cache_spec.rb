require 'spec_helper'

describe "basic rails3 asset test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic-with-cache
      RAILS_ENV: development
    web:
      context: /basic-cache
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should deploy, at least" do
    visit "/basic-cache"
    page.find('#success').should have_content( "It works" )
  end

  it "should perform caching" do
    visit "/basic-cache/root/cachey"
    page.find('#success').should have_content( "crunchy" )
  end

end
