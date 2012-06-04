require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "TorqueBox::Infinispan::Cache sharing across runtimes" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3.2/shared_cache
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work with a persisted cache" do
    visit "/shared-cache/persisted"
    page.find('#success').should have_content( "Ham biscuit" )
  end

  it "should work with an in-memory cache" do
    pending "A fix for in-memory cache sharing"
    visit "/shared-cache"
    page.find('#success').should have_content( "Ham biscuit" )
  end

end


