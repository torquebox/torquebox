require 'spec_helper'

describe "basic rails3.1 test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/rails3.1/basic
      RAILS_ENV: development
    web:
      context: /basic-rails31
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should do a basic get" do
    visit "/basic-rails31"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'basic-rails'
  end

  it "should support injection" do
    visit "/basic-rails31/root/injectiontest"
    find('#success').text.should == 'taco'
  end

  if RUBY_VERSION[0,3] == '1.9'
    it "should support streaming templates" do
      uri = URI.parse(page.driver.send(:url, "/basic-rails31/root/streaming"))
      Net::HTTP.get_response(uri) do |response|
        chunk_count, body = 0, ""
        response.read_body do |chunk|
          chunk_count += 1
          body += chunk
        end
        body.should include('It works')
        chunk_count.should be > 1
      end
    end
  end

end
