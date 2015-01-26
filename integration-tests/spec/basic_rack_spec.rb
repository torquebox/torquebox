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

feature "basic rack at non-root context" do

  torquebox('--dir' => "#{apps_dir}/rack/basic",
            '--context-path' => '/basic-rack',
            '--port' => '8081', '-e' => 'production')

  it "should work for basic requests" do
    visit "/basic-rack"
    page.should have_content('it worked')
    page.find("#success")[:class].strip.should == 'basic-rack'
    page.find("#script_name").text.strip.should == '/basic-rack'
    page.find("#path_info").text.strip.should == ''
    page.find("#request_uri").text.strip.should == '/basic-rack'
  end

  it "should work for subcontext root with trailing slash" do
    visit "/basic-rack/"
    page.should have_content('it worked')
    page.find("#success")[:class].strip.should == 'basic-rack'
    page.find("#script_name").text.strip.should == '/basic-rack'
    page.find("#path_info").text.strip.should == '/'
    page.find("#request_uri").text.strip.should == '/basic-rack/'
  end

  it "should be running under the proper ruby version" do
    visit "/basic-rack/"
    page.find("#ruby-version").text.strip.should == RUBY_VERSION
  end

  it "should not have a vfs path for __FILE__" do
    visit "/basic-rack/"
    page.find("#path").text.strip.should_not match(/^vfs:/)
  end

  it "should not decode characters in URL" do
    visit "/basic-rack/foo%23%2C"
    page.find("#path_info").text.strip.should == '/foo%23%2C'
    page.find("#request_uri").text.strip.should == '/basic-rack/foo%23%2C'
  end

  it "should work for long response bodies" do
    visit '/basic-rack/long_body'
    page.find('#long_body').text.strip.should == 'complete'
  end

  it "should contain correct request headers" do
    uri = URI.parse("#{Capybara.app_host}/basic-rack/")
    Net::HTTP.start(uri.host, uri.port) do |http|
      accept = 'text/html;q=0.9,*/*;q=0.7'
      response = http.get(uri.request_uri, 'Accept' => accept)
      response.code.should == "200"
      response.body.should include("<div id='accept_header'>#{accept}</div>")
    end
  end

  it "should read post bodies via gets" do
    uri = URI.parse("#{Capybara.app_host}/basic-rack/gets")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Post.new(uri.request_uri)
      request.form_data = { 'field' => 'nothing' }
      response = http.request(request)
      response.body.should include(%(<div id='posted'>"field=nothing"</div>))
    end
  end

  it "should read post bodies via read" do
    uri = URI.parse("#{Capybara.app_host}/basic-rack/read")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Post.new(uri.request_uri)
      request.form_data = { 'field' => 'nothing' }
      response = http.request(request)
      response.body.should include(%(<div id='posted'>"field=nothing"</div>))
    end
  end

  it "should read post bodies via each" do
    uri = URI.parse("#{Capybara.app_host}/basic-rack/each")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Post.new(uri.request_uri)
      request.form_data = { 'field' => 'nothing' }
      response = http.request(request)
      response.body.should include(%(<div id='posted'>"field=nothing"</div>))
    end
  end

  it "should read multipart post bodies via gets" do
    visit "/basic-rack/gets"
    attach_file "uploadedfile", "#{apps_dir}/rack/basic/test.txt"
    click_button "submit"
    page.should have_content 'filename=\"test.txt\"\r\nContent-Type'
  end

end

feature "basic rack at root context" do
  torquebox('--dir' => "#{apps_dir}/rack/basic", '--context-path' => '/',
            '-e' => 'production')

  it "should have correct path information" do
    visit "/plaintext"
    page.should have_content('it worked')
    page.find("#success")[:class].strip.should == 'basic-rack'
    page.find("#path_info").text.strip.should == '/plaintext'
    page.find("#request_uri").text.strip.should == '/plaintext'
  end
end

feature "basic rack with Rack::Deflater" do
  torquebox('--dir' => "#{apps_dir}/rack/basic",
            '--context-path' => '/basic-rack',
            '-e' => 'production',
            'deflate_config.ru' => nil)

  it "should work" do
    uri = URI.parse("#{Capybara.app_host}/basic-rack")
    Net::HTTP.start(uri.host, uri.port) do |http|
      request = Net::HTTP::Head.new(uri.request_uri)
      request.add_field('Accept-Encoding', 'gzip,deflate')
      response = http.request(request)
      response.code.should == "200"
      response['vary'].should == 'Accept-Encoding'
      response['content-encoding'].should == 'gzip'
    end
  end
end

if embedded_from_disk?
  feature "basic rack with rackup" do
    rackup(:dir => "#{apps_dir}/rack/basic", '-E' => 'production')

    it "should work for basic requests" do
      visit "/"
      page.should have_content('it worked')
      page.find("#success")[:class].strip.should == 'basic-rack'
      page.find("#script_name").text.strip.should == ''
      page.find("#path_info").text.strip.should == '/'
      page.find("#request_uri").text.strip.should == '/'
    end
  end
end
