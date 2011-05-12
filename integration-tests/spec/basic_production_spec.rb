require 'spec_helper'
require 'fileutils'

describe "basic production" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails2/basic
      RAILS_ENV: production
    web:
      context: /basic-production-rails
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/basic-production-rails"
    element = page.find("#success")
    element[:class].should == "basic-rails"
  end

  it "should cache pages correctly" do
    cache_dir = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'apps/rails2/basic/tmp/cache/views' ) )
    FileUtils.rm_rf( cache_dir )

    visit "/basic-production-rails/cachey?value=taco"
    page.find('#fragment').text.should == 'one-taco-two'
    visit "/basic-production-rails/cachey?value=gouda"
    page.find('#fragment').text.should == 'one-taco-two'

    cache_file = File.join( cache_dir, "localhost.8080", "cachey.cache" )
    File.exist?( cache_file ).should be_true
    File.delete( cache_file )
    File.exist?( cache_file ).should be_false

    visit "/basic-production-rails/cachey?value=jimi"
    page.find('#fragment').text.should == 'one-jimi-two'
    File.exist?( cache_file ).should be_true
  end

end
