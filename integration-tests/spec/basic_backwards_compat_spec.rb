require 'spec_helper'

describe "basic backwards compatibility" do

  deploy "rails2/basic-rails.yml"

  it "should return a basic page" do
    visit "/basic-rails"
    element = page.find('#success')
    element.should_not be_nil
    element[:class].should == "basic-rails"
  end

  it "should send data" do
    visit "/basic-rails/senddata"
    page.source.should == "this is the content"
  end

  it "should send file" do
    visit "/basic-rails/sendfile"
    page.source.chomp.should == "this is the contents of the file"
  end

end
