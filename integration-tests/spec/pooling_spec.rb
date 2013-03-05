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
      root: #{File.dirname(__FILE__)}/../apps/rack/pooling/default
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
      root: #{File.dirname(__FILE__)}/../apps/rack/pooling/default
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

shared_examples_for 'lazy_pool' do

  it 'should be the proper lazy type' do
    mbean("torquebox.pools:name=web,app=#{@app_prefix}_runtime_pooling") do |pool|
      pool.should_not be_nil
      pool.lazy.should == @web_lazy
      wait_for_condition(30, 0.5, lambda { |started| started == !@web_lazy }) do
        pool.started
      end
      pool.started.should == !@web_lazy
    end

    mbean("torquebox.pools:name=messaging,app=#{@app_prefix}_runtime_pooling") do |pool|
      pool.should_not be_nil
      pool.lazy.should == @messaging_lazy
      wait_for_condition(30, 0.5, lambda { |started| started == !@messaging_lazy }) do
        pool.started
      end
      pool.started.should == !@messaging_lazy
    end

    visit '/lazy-pooling/'

    mbean("torquebox.pools:name=web,app=#{@app_prefix}_runtime_pooling") do |pool|
      pool.should_not be_nil
      pool.started.should == true
    end

    mbean("torquebox.pools:name=messaging,app=#{@app_prefix}_runtime_pooling") do |pool|
      pool.should_not be_nil
      pool.started.should == true
    end
  end
end

describe 'lazy runtime pooling' do

    deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/messaging
      env: development
    web:
      context: /lazy-pooling
    ruby:
      version: #{RUBY_VERSION[0,3]}

    pooling:
      web:
        lazy: true
        type: shared
      messaging:
        lazy: true
        type: bounded
        min:  1
        max:  2
  END

  before(:each) do
    @app_prefix = 'lazy'
    @web_lazy = true
    @messaging_lazy = true
  end

  it_should_behave_like 'lazy_pool'
end

describe 'eager runtime pooling' do
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/messaging
      env: development
    web:
      context: /lazy-pooling
    ruby:
      version: #{RUBY_VERSION[0,3]}
    pooling:
      web:
        lazy: false
        type: shared
      messaging:
        lazy: false
        type: bounded
        min:  1
        max:  2
  END

  before(:each) do
    @app_prefix = 'eager'
    @web_lazy = false
    @messaging_lazy = false
  end

  it_should_behave_like 'lazy_pool'
end

describe 'dsl eager runtime pooling' do
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/pooling/dsl_runtime_pooling
      env: development
    web:
      context: /lazy-pooling
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @app_prefix = 'dsl_eager'
    @web_lazy = false
    @messaging_lazy = false
  end

  it_should_behave_like 'lazy_pool'
end

describe 'default runtime pooling' do
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rack/messaging
      env: development
    web:
      context: /lazy-pooling
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  before(:each) do
    @app_prefix = 'default'
    @web_lazy = false
    @messaging_lazy = true
  end

  it_should_behave_like 'lazy_pool'
end
