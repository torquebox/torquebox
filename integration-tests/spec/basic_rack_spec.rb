require 'spec_helper'

describe "basic rack test with filename" do

  deploy "rack/basic-knob.yml"

  it "should work" do
    visit "/basic-rack"
    page.should have_content('it worked')
    page.find("#success")[:class].should == 'basic-rack'
  end

end

describe "basic rack test with heredoc" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/basic
      env: development
    web:
      context: /basic-rack
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work" do
    visit "/basic-rack"
    page.should have_content('it worked')
    page.find("#success")[:class].should == 'basic-rack'
  end

end

describe "basic rack test with hash" do

  deploy( :application => { :root => "#{File.dirname(__FILE__)}/../apps/rack/basic", :env => 'development' },
          :web => { :context => '/basic-rack' },
          :ruby => { :version => RUBY_VERSION[0,3] } )  

  it "should work" do
    visit "/basic-rack"
    page.should have_content('it worked')
    page.find("#success")[:class].should == 'basic-rack'
  end

end
