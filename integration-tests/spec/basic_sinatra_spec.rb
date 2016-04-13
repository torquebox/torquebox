# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'spec_helper'

feature "basic sinatra test" do

  torquebox('--dir' => "#{apps_dir}/sinatra/basic",
            '--context-path' => '/basic-sinatra',
            '-e' => 'production')

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

  it "should return 304 for unmodified static assets" do
    uri = URI.parse("#{Capybara.app_host}/basic-sinatra/some_page.html")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Get.new(uri.request_uri)
      request.add_field('If-Modified-Since', 'Sat, 31 Dec 2050 00:00:00 GMT')
      response = http.request(request)
      response.code.should == "304"
    end
  end

  it "should post something" do
    visit "/basic-sinatra/poster"
    fill_in 'field', :with => 'something'
    click_button 'submit'
    find('#success').should have_content("you posted something")
  end


  it "should allow headers through" do
    uri = URI.parse("#{Capybara.app_host}/basic-sinatra/")
    response = Net::HTTP.get_response(uri)
    response['Biscuit'].should == 'Gravy'
  end

  it "should allow OPTIONS requests" do
    uri = URI.parse("#{Capybara.app_host}/basic-sinatra/")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Options.new(uri.request_uri)
      response = http.request(request)
      response['access-control-allow-origin'].should == '*'
      response['access-control-allow-methods'].should == 'POST'
    end

  end

  it "should test Sir Postalot" do
    uri = URI.parse("#{Capybara.app_host}/basic-sinatra/poster")
    Net::HTTP.start(uri.host, uri.port) do |http|
      100.times do |i|
        http.request(Net::HTTP::Get.new(uri.request_uri))
        request = Net::HTTP::Post.new(uri.request_uri)
        request.form_data = { 'field' => 'nothing' }
        response = http.request(request)
        response.body.should include("<div id='success'>you posted nothing</div>")
      end
    end
  end

  it "should work for long response bodies" do
    visit '/basic-sinatra/long_body'
    page.find('#long_body').text.strip.should == 'complete'
  end

  it "should correctly work for contentless responses" do
    uri = URI.parse("#{Capybara.app_host}/basic-sinatra/304_response")
    Net::HTTP.start(uri.host, uri.port) do |http|
      response = http.request(Net::HTTP::Get.new(uri.request_uri))
      expect(response.code).to eq "304"
      expect(response['Content-Length']).to be nil
      expect(response['Content-Encoding']).to be nil
      expect(response['Transfer-Encoding']).to be nil
    end

  end

end
