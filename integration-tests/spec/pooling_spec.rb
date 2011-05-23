require 'spec_helper'

shared_examples_for "configured pool" do
  before(:each) do
    visit "/pooling-#{@suffix}"
    page.should have_content('it worked')
  end

  it "should be the proper pool type" do
    puts page.find("#pool-class").text
    page.find("#pool-class").text.should == @runtime_class
  end

end

describe "shared runtime pooling" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/pooling
      env: development
    web:
      context: /pooling-shared
    ruby:
      version: #{RUBY_VERSION[0,3]}

    pooling:
      web: shared
  END

  before(:each) do
    @suffix = 'shared'
    @runtime_class = 'org.torquebox.core.runtime.SharedRubyRuntimePool'
  end

  it_should_behave_like "configured pool"
end

describe "bounded runtime pooling" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/pooling
      env: development
    web:
      context: /pooling-bounded
    ruby:
      version: #{RUBY_VERSION[0,3]}

    pooling:
      web:
        min: 1
        max: 42
  END

  before(:each) do
    @suffix = 'bounded'
    @runtime_class = 'org.torquebox.core.runtime.DefaultRubyRuntimePool'
  end

  it_should_behave_like "configured pool"

  describe "min/max settings" do
    before(:each) do
      visit "/pooling-bounded"
      page.should have_content('it worked')
    end
    
    it "should have the proper min setting" do
      page.find("#pool-min").text.should == '1'
    end
    
    it "should have the proper max setting" do
      page.find("#pool-max").text.should == '42'
    end
  end
end

