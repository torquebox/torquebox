require 'spec_helper'

describe "rack vendor/jars/-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/rack/vendor-jars-jar
      RACK_ENV: development
    web:
      context: /vendor-jars-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
