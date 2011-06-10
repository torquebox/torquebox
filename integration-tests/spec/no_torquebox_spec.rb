require 'spec_helper'

describe "no torquebox tests" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/no_torquebox
    web:
      context: /no-torquebox
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should not throw error about requiring torquebox-messaging" do
    visit "/no-torquebox"
    page.should have_content('It works')
  end
end
