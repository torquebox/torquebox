require 'spec_helper'

describe "exposing app name to rack app" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/app_name
      env: development
    web:
      context: /app_name
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "set the constant and env var" do
    visit "/app_name"
    puts "JC:", page.source
    page.should have_content('constant:exposing_app_name_to_rack_app|env:exposing_app_name_to_rack_app')
  end
end
