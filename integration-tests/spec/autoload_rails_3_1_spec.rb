require 'spec_helper'

describe "autoload rails3.1 test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/autoload
      RAILS_ENV: development
    web:
      context: /autoload
    
    ruby:
      version: 1.9
  END

  it "should do a get to the test page" do
    visit "/autoload/test/test"
    page.should have_content('Called test')
  end

end
