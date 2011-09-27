require 'spec_helper'

describe "rails PWD-based jar loading" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/pwd-jar
      RAILS_ENV: development
    web:
      context: /pwd-jar
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/pwd-jar"
    uuid = page.find( '#success' ).text
    uuid.should match(/^........-....-....-....-............$/)
  end

  before(:each) do
    @default_dir = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
    FileUtils.rm_rf @default_dir
  end

  after(:all) do
    FileUtils.rm_rf @default_dir
  end

end
