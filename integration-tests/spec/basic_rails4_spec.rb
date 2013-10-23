require 'spec_helper'

describe 'basic rails4 test' do

  # Rails 4 is 1.9+
  if RUBY_VERSION >= '1.9'
    mutable_app 'rails4/basic'
    deploy <<-END.gsub(/^ {6}/,'')
      ---
      application:
        RAILS_ROOT: #{File.dirname(__FILE__)}/../target/apps/rails4/basic
        RAILS_ENV: development
      web:
        context: /basic-rails4
      ruby:
        version: #{RUBY_VERSION[0,3]}
    END

    it 'should do a basic get' do
      visit '/basic-rails4'
      page.should have_content('It works')
      page.find('#success')[:class].should == 'basic-rails4'
    end

    context 'streaming' do
      pending 'jruby 1.7.x fiber fixes' do
        it "should work for small responses" do
          verify_streaming("/basic-rails4/root/streaming?count=0")
        end

        it "should work for large responses" do
          verify_streaming("/basic-rails4/root/streaming?count=500")
        end
      end

      def verify_streaming(url)
        uri = URI.parse(page.driver.send(:url, url))
        Net::HTTP.get_response(uri) do |response|
          response.should be_chunked
          response.header['transfer-encoding'].should == 'chunked'
          chunk_count, body = 0, ""
          response.read_body do |chunk|
            chunk_count += 1
            body += chunk
          end
          body.should include('It works')
          body.each_line do |line|
            line.should_not match(/^\d+\s*$/)
          end
          chunk_count.should be > 1
        end
      end
    end

    it 'should support class reloading' do
      visit '/basic-rails4/reloader/0'
      element = page.find_by_id('success')
      element.should_not be_nil
      element.text.should == 'INITIAL'

      seen_values = Set.new
      seen_values << element.text
      counter = 1
      while seen_values.size <= 3 && counter < 60 do
        visit "/basic-rails4/reloader/#{counter}"
        element = page.find_by_id('success')
        element.should_not be_nil
        seen_values << element.text
        counter += 1
      end

      seen_values.size.should > 3
    end

    it 'should return a static page beneath default public dir' do
      visit "/basic-rails4/some_page.html"
      element = page.find('#success')
      element.should_not be_nil
      element.text.should == 'static page'
    end

    it "should support setting multiple cookies" do
      visit "/basic-rails4/root/multiple_cookies"
      page.driver.cookies['foo1'].value.should == 'bar1'
      page.driver.cookies['foo2'].value.should == 'bar2'
      page.driver.cookies['foo3'].value.should == 'bar3'
    end

  end
end
