require 'spec_helper'

shared_examples_for "session handling" do

  def assert_success(value)
    find('#success').text.strip.should == value
  end

  def assert_success_and_cookies(value)
    cookies = page.driver.cookies
    cookies.count.should == 1
    cookies['JSESSIONID'].value.should == @session_id
    assert_success(value)
  end

  it "should behave reasonably" do
    page.driver.cookies.count.should == 0

    visit "/basic-rails/sessioning/get_value"
    @session_id = page.driver.cookies['JSESSIONID'].value
    @session_id.should_not be_nil
    assert_success_and_cookies("")

    visit "/basic-rails/sessioning/set_value"
    assert_success_and_cookies("the value")
    visit "/basic-rails/sessioning/get_value"
    assert_success_and_cookies("the value")
    visit "/basic-rails/sessioning/clear_value"
    assert_success_and_cookies("")
    visit "/basic-rails/sessioning/get_value"
    assert_success_and_cookies("")
  end

  it "should reset session data after logout" do
    page.driver.cookies.count.should == 0
    visit "/basic-rails/sessioning/get_value"
    assert_success("")
    visit "/basic-rails/sessioning/set_value"
    assert_success("the value")
    visit "/basic-rails/sessioning/get_value"
    assert_success("the value")
    visit "/basic-rails/sessioning/logout"
    visit "/basic-rails/sessioning/get_value"
    assert_success("")
  end

  it "should work via matrix url" do
    page.driver.cookies.count.should == 0
    visit "/basic-rails/sessioning/get_value"
    assert_success("")
    visit "/basic-rails/sessioning/set_value"
    assert_success("the value")
    visit "/basic-rails/sessioning/get_value"
    assert_success("the value")

    session_id = page.driver.cookies['JSESSIONID'].value
    session_id.length.should be > 0
    
    page.driver.cookies.clear

    visit "/basic-rails/sessioning/get_value;jsessionid=#{session_id}"
    assert_success("the value")

    find('#session_id').text.should == session_id
    page.driver.cookies.count.should == 0
  end

  it "should support session language crossing" do
    page.driver.cookies.count.should == 0
    visit "/basic-rails/sessioning/logout"
    visit "/basic-rails/sessioning/set_from_ruby"
    visit "/basic-rails/sessioning/display_session"
    find('#a_fixnum_ruby').text.should == '42'
    find('#a_fixnum_java').text.should == '42'
    find('#a_string_ruby').text.should == 'swordfish'
    find('#a_string_java').text.should == 'swordfish'
    find('#a_boolean_ruby').text.should == 'true'
    find('#a_boolean_java').text.should == 'true'
    find('#a_string_key_ruby').text.should == 'tacos'
    find('#a_string_key_java').text.should == 'tacos'
  end

  it "should reset and restore" do
    visit "/basic-rails/sessioning/get_value"
    assert_success("")
    visit "/basic-rails/sessioning/set_value"
    assert_success("the value")
    visit "/basic-rails/sessioning/get_value"
    assert_success("the value")
    visit "/basic-rails/sessioning/reset_and_restore"
    find('#success').should_not be_nil
  end

end

describe "rails2 sessions" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: development
    web:
      context: /basic-rails
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END
  it_should_behave_like "session handling"
end
describe "rails3 sessions" do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic
      RAILS_ENV: development
    web:
      context: /basic-rails
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END
  it_should_behave_like "session handling"
end
