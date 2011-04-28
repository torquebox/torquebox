require 'spec_helper'

describe "basic sinatra test" do

  deploy "sinatra/basic-sinatra-knob.yml"

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
