require 'spec_helper'

describe "sprockets 2.1 and rails3.1 test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/sprockets-2.1
      RAILS_ENV: development
    web:
      context: /sprockets-2.1
    
    ruby:
      version: 1.9
  END

  it "should do a basic get" do
    visit "/sprockets-2.1/home/index"
    page.should have_content('Home#index')
    page.find("#success")[:class].should == 'sprockets'
  end

end
