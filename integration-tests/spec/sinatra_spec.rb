require 'spec_helper'

describe "sinatra context without trailing slash" do
  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/context
      env: development
    web:
      context: /sinatra-context
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should return index" do
    visit "/sinatra-context"
    page.should have_content('Index')
  end

  it "should return something" do
    visit "/sinatra-context/something"
    page.should have_content('Something')
  end
end

describe "sinatra context with trailing slash" do
  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/context
      env: development
    web:
      context: /sinatra-context/
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should return index" do
    visit "/sinatra-context"
    page.should have_content('Index')
  end

  it "should return something" do
    visit "/sinatra-context/something"
    page.should have_content('Something')
  end
end