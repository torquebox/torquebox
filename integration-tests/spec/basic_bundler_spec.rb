require 'spec_helper'

remote_describe "basic bundler test with rack" do

  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic_bundler
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work with an old version of rack gem" do
    require 'bundler/setup'
  end

end
