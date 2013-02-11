require 'spec_helper'
require 'time'

describe "sinatra with TorqueBox sessions" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/sessions
      env: development
    web:
      context: /sinatra-sessions
      session_timeout: 2m
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should retain session data after redirect" do
    visit "/sinatra-sessions/foo"
    page.driver.cookies['JSESSIONID'].value.should_not be_nil
    page.should have_content('Hello World!')
  end

  it "should timeout after specified time" do
    visit "/sinatra-sessions/inactive_interval"
    page.should have_content('120')
  end

end

describe "sinatra with TorqueBox session options" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/sessions
      env: development
    web:
      context: /sinatra-sessions2
    environment:
      ALL_SESSION_COOKIE_OPTIONS: true
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should set session options" do
    uri = URI.parse(page.driver.send(:url, "/sinatra-sessions2/inactive_interval"))
    Net::HTTP.get_response(uri) do |response|
      cookie = response.header['set-cookie'].split(';').map(&:strip)
      cookie.length.should == 6
      cookie[0].start_with?('sinatra_sessions').should be_true
      cookie[1].should == 'Domain=foobar.com'
      expires = Time.parse(cookie[2].sub('Expires=', ''))
      (expires - Time.now).should be_within(20).of(60)
      cookie[3].should == 'Path=/baz'
      cookie[4].should == 'Secure'
      cookie[5].should == 'HttpOnly'
      response.body.should == '180'
    end
  end

end
