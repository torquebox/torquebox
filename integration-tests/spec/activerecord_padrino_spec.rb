require 'spec_helper'

describe "activerecord padrino test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/padrino/activerecord
      RAILS_ENV: development
    web:
      context: /activerecord-padrino
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should do a basic get" do
    visit "/activerecord-padrino"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'activerecord-padrino'
  end

  it "should be able to login to the admin console" do
    visit "/activerecord-padrino/admin"
    page.should have_content('Padrino Admin')
  end

end
