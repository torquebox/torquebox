require 'spec_helper'

asset_root = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic
      RAILS_ENV: development
    web:
      context: /
      host: integ-app3.torquebox.org
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

asset_context = <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3/basic
      RAILS_ENV: development
    web:
      context: basic-rails3-asset
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

describe "basic rails3 asset test" do

  deploy asset_root, asset_context

  before(:each) do
    @original_capy_app_host = Capybara.app_host
  end

  after(:each) do
    Capybara.app_host = @original_capy_app_host
  end

  describe "root context" do
    before(:each) do
      Capybara.app_host = "http://integ-app3.torquebox.org:8080"
    end

    it "should generate correct asset and link paths" do
      visit "/"
      image = page.find('img')
      image['src'].should match(/\/images\/rails\.png/)
      link = page.find('a')
      link['href'].should eql('/')
    end

    it "should return correct Content-Type header", :browser_not_supported=>true do
      visit "/images/rails.png"
      page.response_headers['Content-Type'].should == 'image/png'
    end

    # Duplicated here to explicitly ensure page caching works at root context
    it 'should support rails page caching with html' do
      visit "/root/page_caching?time=#{Time.now.to_f}"
      element = page.find_by_id('success')
      element.should_not be_nil
      first_time = element.text
      visit "/root/page_caching?time=#{Time.now.to_f}"
      element = page.find_by_id('success')
      element.should_not be_nil
      second_time = element.text
      first_time.should == second_time
      visit "/root/expire_page_cache"
      visit "/root/page_caching?time=#{Time.now.to_f}"
      element = page.find_by_id('success')
      element.should_not be_nil
      third_time = element.text
      third_time.should_not == second_time
      visit "/root/expire_page_cache"
    end

    it 'should return a static page beneath default public dir' do
      visit "/some_page.html"
      element = page.find('#success')
      element.should_not be_nil
      element.text.should == 'static page'
    end
  end

  describe "non-root context" do
    it "should generate correct asset and link paths" do
      visit "/basic-rails3-asset"
      image = page.find('img')
      image['src'].should match(/\/basic-rails3-asset\/images\/rails\.png/)
      link = page.find('a')
      link['href'].should eql('/basic-rails3-asset/')
    end

    it "should return correct Content-Type header", :browser_not_supported=>true do
      visit "/basic-rails3-asset/images/rails.png"
      page.response_headers['Content-Type'].should == 'image/png'
    end

    it 'should return a static page beneath default public dir' do
      visit "/basic-rails3-asset/some_page.html"
      element = page.find('#success')
      element.should_not be_nil
      element.text.should == 'static page'
    end
  end

end
