require 'spec_helper'
require 'set'

describe "rack reloading" do
  mutable_app 'rack/reloader'
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../target/apps/rack/reloader
      RACK_ENV: development
    web:
      context: /reloader-rack
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should reload" do
    visit "/reloader-rack?0"
    element = page.find_by_id("success")
    element.should_not be_nil
    element.text.should == 'INITIAL'

    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 60 do
      visit "/reloader-rack?#{counter}"
      element = page.find_by_id("success")
      element.should_not be_nil
      seen_values << element.text
      counter += 1
    end

    seen_values.size.should > 3
  end
  
end
