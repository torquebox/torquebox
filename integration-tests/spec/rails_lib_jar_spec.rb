require 'spec_helper'

describe "rails lib/-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/lib-jar
      RAILS_ENV: development
    web:
      context: /lib-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/lib-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

end
