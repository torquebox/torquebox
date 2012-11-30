require 'spec_helper'
require 'set'

describe 'basic rails3.2 test' do
  mutable_app 'rails3.2/basic'

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../target/apps/rails3.2/basic
      RAILS_ENV: development
    web:
      context: /basic-rails32
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it 'should do a basic get' do
    visit '/basic-rails32'
    page.should have_content('It works')
    page.find('#success')[:class].should == 'basic-rails3.2'
  end

  it 'should support class reloading' do
    visit '/basic-rails32/reloader/0'
    element = page.find_by_id('success')
    element.should_not be_nil
    element.text.should == 'INITIAL'

    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 60 do
      visit "/basic-rails32/reloader/#{counter}"
      element = page.find_by_id('success')
      element.should_not be_nil
      seen_values << element.text
      counter += 1
    end

    seen_values.size.should > 3
  end

  it 'should support rails page caching with html' do
    visit "/basic-rails32/root/page_caching?time=#{Time.now.to_f}"
    element = page.find_by_id('success')
    element.should_not be_nil
    first_time = element.text
    visit "/basic-rails32/root/page_caching?time=#{Time.now.to_f}"
    element = page.find_by_id('success')
    element.should_not be_nil
    second_time = element.text
    first_time.should == second_time
    visit "/basic-rails32/root/expire_page_cache"
    visit "/basic-rails32/root/page_caching?time=#{Time.now.to_f}"
    element = page.find_by_id('success')
    element.should_not be_nil
    third_time = element.text
    third_time.should_not == second_time
  end

  it 'should support rails page caching with json' do
    visit "/basic-rails32/root/page_caching.json?time=#{Time.now.to_f}"
    first_time = JSON.parse(page.source)['time']
    visit "/basic-rails32/root/page_caching.json?time=#{Time.now.to_f}"
    second_time = JSON.parse(page.source)['time']
    first_time.should == second_time
    visit "/basic-rails32/root/expire_page_cache.json"
    visit "/basic-rails32/root/page_caching.json?time=#{Time.now.to_f}"
    third_time = JSON.parse(page.source)['time']
    third_time.should_not == second_time
  end

  it 'should return a static page beneath default public dir' do
    visit "/basic-rails32/some_page.html"
    element = page.find('#success')
    element.should_not be_nil
    element.text.should == 'static page'
  end

  it "should support setting multiple cookies" do
    visit "/basic-rails32/root/multiple_cookies"
    page.driver.cookies['foo1'].value.should == 'bar1'
    page.driver.cookies['foo2'].value.should == 'bar2'
    page.driver.cookies['foo3'].value.should == 'bar3'
  end

  it 'should use config.ru' do
    visit '/basic-rails32'
    page.find('#rackup_file').text.should == 'config.ru'
  end

end

describe 'basic rails3.2 test with alternate rackup file' do
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.2/basic
      RAILS_ENV: development
    web:
      context: /basic-rails32
      rackup: alternate_config.ru
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it 'should use alternate_config.ru' do
    visit '/basic-rails32'
    page.find('#rackup_file').text.should == 'alternate_config.ru'
  end
end
