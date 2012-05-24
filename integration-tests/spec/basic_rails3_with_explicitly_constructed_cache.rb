require 'spec_helper'

describe "basic rails3 with explicitly constructed cache test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic-with-explicit-cache
      RAILS_ENV: development
    web:
      context: /basic-with-explicit-cache
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    pending "A fix for TORQUE-830"
    visit "/basic-with-explicit-cache"
    page.find("#success").should have_content( "TorqueBoxStore" )
  end

end

