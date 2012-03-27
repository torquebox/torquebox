require 'spec_helper'

describe "rails app with netty.jar" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/break-jar
      RAILS_ENV: development
    web:
      context: /break-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/break-jar"
    page.find( '#success' ).text
  end

end
