require 'spec_helper'

describe "rack PWD-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/rack/pwd-jar
      RACK_ENV: development
    web:
      context: /pwd-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/pwd-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
