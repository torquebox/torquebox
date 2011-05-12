require 'spec_helper'

describe "sinatra with TorqueBox sessions" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/sessions
      env: development
    web:
      context: /sinatra-sessions
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should retain session data after redirect" do
    visit "/sinatra-sessions/foo"
    page.driver.cookies['JSESSIONID'].value.should_not be_nil
    page.should have_content('Hello World!')
  end

end
