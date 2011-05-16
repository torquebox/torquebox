require 'spec_helper'

describe "basic rails3 test" do

  deploy "rails3/basic-knob.yml"

  it "should do a basic get" do
    visit "/basic-rails"
    page.should have_content('It works')
    page.find("#success")[:class].should == 'basic-rails'
  end

  it "should do a raw post" do
    visit "/basic-rails/post/raw"
    fill_in "name", :with => "my-name"
    click_button "submit"
    find("#raw_post").should have_content("name=my-name")
    find("#name").should have_content("my-name")
  end

  it "should support injection" do
    visit "/basic-rails/root/injectiontest"
    find('#success').text.should == 'taco'
  end

end
