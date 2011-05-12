require 'spec_helper'

describe "rack lib/-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/rack/lib-jar
      RACK_ENV: development
    web:
      context: /lib-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/lib-jar"
    uuid = page.find( :xpath, '//body' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
