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

end
