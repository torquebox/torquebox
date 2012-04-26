require 'spec_helper'

describe "basic sinatra test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/sinatra/basic
      RACK_ENV: development
    web:
      context: /basic-sinatra
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should work" do
    visit "/basic-sinatra"
    page.should have_content('it worked')
    page.should have_selector('div.sinatra-basic')
    find("#success").should have_content('it worked')
  end

  it "should return a valid request scheme" do
    visit "/basic-sinatra/request-mapping"
    find("#success #scheme").text.should eql("http")
  end

  it "should return a static page beneath default 'public' dir" do
    visit "/basic-sinatra/some_page.html"
    page.find('#success')[:class].should == 'default'
  end

  it "should post something" do
    visit "/basic-sinatra/poster"
    fill_in 'field', :with => 'something'
    click_button 'submit'
    find('#success').should have_content("you posted something")
  end

  
  it "should allow headers through (JRUBY-5839, TORQUE-430)", :browser_not_supported=>true do
    visit "/basic-sinatra"
    page.response_headers['Biscuit'].should == 'Gravy'
  end

  it "should allow OPTIONS requests (TORQUE-792)", :browser_not_supported => true do
    uri = URI.parse(page.driver.send(:url, "/basic-sinatra/"))
    http = Net::HTTP.new(uri.host, uri.port)
    request = Net::HTTP::Options.new(uri.request_uri)
    response = http.request(request)
    response['access-control-allow-origin'].should == '*'
    response['access-control-allow-methods'].should == 'POST'

  end
  
  it "should test Sir Postalot" do
    500.times do |i|
      print '.' if (i % 10 == 0)
      visit "/basic-sinatra/poster"
      click_button 'submit'
      find('#success').text.should == "you posted nothing"
    end
    puts " complete!"
  end

end
