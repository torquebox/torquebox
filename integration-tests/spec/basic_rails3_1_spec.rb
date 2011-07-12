require 'spec_helper'

describe "basic rails3.1 test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/basic
      RAILS_ENV: development
    web:
      context: /basic-rails31
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should do a basic get" do
    visit "/basic-rails31"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'basic-rails'
  end

  it "should support injection" do
    visit "/basic-rails31/root/injectiontest"
    find('#success').text.should == 'taco'
  end

end
