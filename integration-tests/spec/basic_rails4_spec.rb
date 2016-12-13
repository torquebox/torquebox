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

feature 'basic rails4 test' do

  torquebox('--dir' => "#{apps_dir}/rails4/basic",
            '--context-path' => '/basic-rails4',
            '-e' => 'production')

  it 'should do a basic get' do
    visit '/basic-rails4'
    expect(page).to have_content('It works')
    expect(page.find('#success')[:class]).to eq('basic-rails4')
  end

  context 'streaming' do
    # Ignore this spec for now until repeated Travis failures can be
    # resolved. I think there's a race condition in JRuby's Fiber impl
    # somewhere, but have yet to track it down.
    xit "should work for small responses" do
      verify_streaming("/basic-rails4/root/streaming?count=0")
    end

    xit "should work for large responses" do
      verify_streaming("/basic-rails4/root/streaming?count=500")
    end

    def verify_streaming(url)
      uri = URI.parse("#{Capybara.app_host}#{url}")
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.open_timeout = 10
      http.start do |request|
        request.request_get(uri.request_uri) do |response|
          expect(response).to be_chunked
          expect(response.header['transfer-encoding']).to eq('chunked')
          chunk_count, body = 0, ""
          response.read_body do |chunk|
            chunk_count += 1
            body += chunk
          end
          expect(body).to include('It works')
          body.each_line do |line|
            expect(line).not_to match(/^\d+\s*$/)
          end
          expect(chunk_count).to be > 1
        end
      end
    end
  end

  context "server sent events" do
    it "should work" do
      uri = URI.parse("#{Capybara.app_host}/basic-rails4/live/sse")
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.open_timeout = 10
      http.start do |request|
        request.request_get(uri.request_uri) do |response|
          chunk_count, body = 0, ""
          response.read_body do |chunk|
            chunk_count += 1
            body += chunk
          end
          expect(body).to include('test1')
          expect(body).to include('test4')
          expect(chunk_count).to be > 3
        end
      end
    end
  end

  it 'should return a static page beneath default public dir' do
    visit "/basic-rails4/some_page.html"
    element = page.find('#success')
    expect(element).not_to be_nil
    expect(element.text).to eq('static page')
  end

  it "should support setting multiple cookies" do
    visit "/basic-rails4/root/multiple_cookies"
    expect(page.driver.cookies['foo1'].value).to eq('bar1')
    expect(page.driver.cookies['foo2'].value).to eq('bar2')
    expect(page.driver.cookies['foo3'].value).to eq('bar3')
  end

  it "should serve assets from app/assets" do
    visit "/basic-rails4/assets/test.js?body=1"
    page.source.should =~ %r{// taco}
  end

  it "should generate correct asset and link paths" do
    visit "/basic-rails4"
    image = page.find('img')
    image['src'].should match(%r{/basic-rails4/assets/rails\.png})
    link = page.find('a')
    link['href'].should include('/basic-rails4/')
  end

end
