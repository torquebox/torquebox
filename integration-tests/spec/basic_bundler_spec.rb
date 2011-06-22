require 'spec_helper'

describe "basic bundler test with rack" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic_bundler
      env: development
    web:
      context: /basic-bundler
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work with an old version of rack gem" do
    visit "/basic-bundler"
    page.should have_content('it worked')
  end
end
