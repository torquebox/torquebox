require 'spec_helper'

describe "jruby profile.api and rails3.1 test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/profile
      RAILS_ENV: development
    web:
      context: /profile
    
    ruby:
      version: 1.9
  END

  it "should use the profile api successfully" do
    visit "/profile/test/index"
    page.should have_content('Total time:')
    page.should have_content('%total')
    page.should have_content('%self')
    page.should have_content('children')
    page.should have_content('calls')
    page.should have_content('name')
  end

end
