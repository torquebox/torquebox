require 'spec_helper'

describe "basic rails3 asset test" do

  deploy "rails3/basic-with-cache-knob.yml"

  it "should deploy, at least" do
    visit "/basic-cache"
    page.find('#success').should have_content( "It works" )
  end

  it "should perform caching" do
    visit "/basic-cache/root/cachey"
    page.find('#success').should have_content( "crunchy" )
  end

end
