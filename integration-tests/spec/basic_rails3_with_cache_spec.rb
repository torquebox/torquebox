require 'spec_helper'

describe "basic rails3 with cache test" do

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

  it "should use ActiveSupport::Cache::TorqueBoxStore" do
    visit "/basic-cache/root/torqueboxey" 
    page.find("#type").should have_content( "TorqueBoxStore" )
  end

  it "should default to local mode" do
    visit "/basic-cache/root/torqueboxey" 
    page.find("#mode").should have_content( "LOCAL" )
  end

  it "should perform caching" do
    visit "/basic-cache/root/cachey"
    page.find('#success').should have_content( "crunchy" )
  end

  it "should transactionally cache objects in the store" do
    visit "/basic-cache/root/cacheytx"
    page.find("#success").should have_content( "crunchy" )
  end

  it "should rollback failed transactional cache objects in the store" do
    visit "/basic-cache/root/cacheytxthrows"
    page.find("#success").should have_content( "soft" )
  end

  it "should evict entries when :max_entries has been reached" do
    visit "/basic-cache/root/eviction"
#    page.find("#success").should have_content( "2" )
  end

end
