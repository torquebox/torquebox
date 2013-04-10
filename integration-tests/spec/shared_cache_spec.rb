require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

shared_examples_for "cache sharing across runtimes" do
  it "should work with a persisted cache" do
    visit "/shared-cache/persisted"
    page.find('#success').should have_content( "Ham biscuit" )
  end

  it "should work with an in-memory cache" do
    visit "/shared-cache"
    page.find('#success').should have_content( "Ham biscuit" )
  end
end

describe "marshal_base64 encoding" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3.2/shared_cache
      env: development
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
      CACHE_ENCODING: marshal_base64
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "cache sharing across runtimes"
end

describe "marshal_smart encoding" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3.2/shared_cache
      env: development
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
      CACHE_ENCODING: marshal_smart
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "cache sharing across runtimes"
end

describe "marshal encoding" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3.2/shared_cache
      env: development
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
      CACHE_ENCODING: marshal
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "cache sharing across runtimes"
end


