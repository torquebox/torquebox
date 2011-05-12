require 'spec_helper'

describe "rails vendor/jars/-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/vendor-jars-jar
      RAILS_ENV: development
    web:
      context: /vendor-jars-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/vendor-jars-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
