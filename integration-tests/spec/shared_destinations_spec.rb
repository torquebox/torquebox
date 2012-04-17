require 'spec_helper'

foo = <<-FOO.gsub(/^ {4}/, '')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic
    web:
      context: /foo
    queues:
      /queues/test:
        durable: false
    topics:
      /topics/test:
        durable: false
    ruby:
      version: #{RUBY_VERSION[0,3]}
FOO
  
bar = <<-BAR.gsub(/^ {4}/, '')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic
    web:
      context: /bar
    queues:
      /queues/test:
        durable: false
    topics:
      /topics/test:
        durable: false
    ruby:
      version: #{RUBY_VERSION[0,3]}
BAR

describe "shared destinations test" do

  deploy foo, bar

  it "should work with foo" do
    visit "/foo"
    page.should have_content('it worked')
  end

  it "should work with bar" do
    visit "/bar"
    page.should have_content('it worked')
  end

end

