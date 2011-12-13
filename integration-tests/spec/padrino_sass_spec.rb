require 'spec_helper'

describe "padrino sass test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/padrino/padrino-sass
      RAILS_ENV: development
    web:
      context: /padrino-sass
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should do a basic get" do
    visit "/padrino-sass/foo"
    page.should have_content('It works')
  end

end
