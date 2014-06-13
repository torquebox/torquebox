require 'spec_helper'

feature 'basic rails4 test' do

  torquebox('--dir' => "#{apps_dir}/rails4/basic",
            '--context-path' => '/basic-rails4',
            '-E' => 'production')

  it 'should do a basic get' do
    visit '/basic-rails4'
    expect(page).to have_content('It works')
    expect(page.find('#success')[:class]).to eq('basic-rails4')
  end

  context 'streaming' do
    it "should work for small responses" do
      verify_streaming("/basic-rails4/root/streaming?count=0")
    end

    it "should work for large responses" do
      verify_streaming("/basic-rails4/root/streaming?count=500")
    end

    def verify_streaming(url)
      uri = URI.parse("#{Capybara.app_host}#{url}")
      Net::HTTP.get_response(uri) do |response|
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
    page.source.should =~ /\/\/ taco/
  end

  it "should generate correct asset and link paths" do
    visit "/basic-rails4"
    image = page.find('img')
    image['src'].should match(/\/basic-rails4\/assets\/rails\.png/)
    link = page.find('a')
    link['href'].should eql('/basic-rails4/')
  end

end
