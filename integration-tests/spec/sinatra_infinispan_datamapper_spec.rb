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

  it "should work, according to lanceball" do
    visit "/sinatra-datamapper"
    puts "", "PAGE: #{page.body}"
    page.should have_content('It Works!')
  end

end

